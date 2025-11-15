package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.smartattendance.model.dto.dashboard.AttendanceRecord;
import com.smartattendance.model.dto.dashboard.DashboardFilter;
import com.smartattendance.model.dto.dashboard.DashboardTopCards;
import com.smartattendance.config.DatabaseUtil;

public class JdbcDashboardRepository implements DashboardRepository {

    @Override
    public List<AttendanceRecord> findAttendance(DashboardFilter filter) throws SQLException {
        List<AttendanceRecord> out = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT a.marked_at, a.status, a.method, a.confidence, a.note, ")
                .append("a.user_id, u.username, a.session_id, ")
                .append("s.session_date, s.start_time, s.course_id, c.course_code ")
                .append("FROM attendance a ")
                .append("LEFT JOIN users u ON a.user_id = u.user_id ")
                .append("LEFT JOIN sessions s ON a.session_id = s.session_id ")
                .append("LEFT JOIN courses c ON s.course_id = c.course_id ")
                .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (filter.from != null) {
            sb.append("AND a.marked_at::date >= ? ");
            params.add(Date.valueOf(filter.from));
        }
        if (filter.to != null) {
            sb.append("AND a.marked_at::date <= ? ");
            params.add(Date.valueOf(filter.to));
        }
        if (filter.courseId != null) {
            sb.append("AND s.course_id = ? ");
            params.add(filter.courseId);
        }
        if (filter.sessionId != null) {
            sb.append("AND a.session_id = ? ");
            params.add(filter.sessionId);
        }

        sb.append("ORDER BY a.marked_at DESC");

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sb.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRecord r = new AttendanceRecord();
                    Timestamp markedTs = rs.getTimestamp("marked_at");
                    if (markedTs != null) {
                        r.markedAt = markedTs.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                    }
                    r.status = rs.getString("status");
                    r.method = rs.getString("method");
                    r.confidence = (Double) rs.getObject("confidence");
                    r.note = rs.getString("note");
                    r.userId = (Integer) rs.getObject("user_id");
                    r.username = rs.getString("username");
                    r.sessionId = (Integer) rs.getObject("session_id");

                    Date sd = rs.getDate("session_date");
                    if (sd != null) {
                        r.sessionDate = sd.toLocalDate();
                    }
                    Timestamp st = rs.getTimestamp("start_time");
                    if (st != null) {
                        r.sessionStart = st.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                    }
                    r.courseId = (Integer) rs.getObject("course_id");
                    r.courseCode = rs.getString("course_code");
                    out.add(r);
                }
            }
        }

        return out;
    }

    @Override
    public DashboardTopCards computeTopCards(DashboardFilter filter) throws SQLException {
        int students = 0;
        int sessions = 0;
        int present = 0;

        List<String> wantedStatuses = new ArrayList<>();
        if (filter.includePresent)
            wantedStatuses.add("Present");
        if (filter.includeLate)
            wantedStatuses.add("Late");
        if (filter.includeAbsent)
            wantedStatuses.add("Absent");
        if (filter.includePending)
            wantedStatuses.add("Pending");

        try (Connection conn = DatabaseUtil.getConnection()) {
            // students
            StringBuilder stu = new StringBuilder(
                    "SELECT COUNT(DISTINCT a.user_id) AS cnt " +
                            "FROM attendance a " +
                            "LEFT JOIN sessions s ON a.session_id = s.session_id " +
                            "WHERE 1=1 ");
            List<Object> params = new ArrayList<>();
            if (filter.from != null) {
                stu.append("AND a.marked_at::date >= ? ");
                params.add(Date.valueOf(filter.from));
            }
            if (filter.to != null) {
                stu.append("AND a.marked_at::date <= ? ");
                params.add(Date.valueOf(filter.to));
            }
            if (filter.courseId != null) {
                stu.append("AND s.course_id = ? ");
                params.add(filter.courseId);
            }
            if (filter.sessionId != null) {
                stu.append("AND a.session_id = ? ");
                params.add(filter.sessionId);
            }
            if (!wantedStatuses.isEmpty()) {
                stu.append("AND a.status IN (");
                for (int i = 0; i < wantedStatuses.size(); i++) {
                    stu.append("?");
                    if (i < wantedStatuses.size() - 1)
                        stu.append(",");
                }
                stu.append(") ");
            }

            try (PreparedStatement ps = conn.prepareStatement(stu.toString())) {
                int idx = 1;
                for (Object o : params)
                    ps.setObject(idx++, o);
                for (String s : wantedStatuses)
                    ps.setString(idx++, s);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        students = rs.getInt("cnt");
                }
            }

            if (students == 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM users WHERE role = 'STUDENT'")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next())
                            students = rs.getInt(1);
                    }
                }
            }

            // sessions
            if (filter.sessionId != null) {
                sessions = 1;
            } else {
                StringBuilder ssb = new StringBuilder("SELECT COUNT(*) FROM sessions WHERE 1=1 ");
                List<Object> p2 = new ArrayList<>();
                if (filter.from != null) {
                    ssb.append("AND session_date >= ? ");
                    p2.add(Date.valueOf(filter.from));
                }
                if (filter.to != null) {
                    ssb.append("AND session_date <= ? ");
                    p2.add(Date.valueOf(filter.to));
                }
                if (filter.courseId != null) {
                    ssb.append("AND course_id = ? ");
                    p2.add(filter.courseId);
                }

                try (PreparedStatement ps = conn.prepareStatement(ssb.toString())) {
                    for (int i = 0; i < p2.size(); i++)
                        ps.setObject(i + 1, p2.get(i));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next())
                            sessions = rs.getInt(1);
                    }
                }
            }

            // present
            if (filter.includePresent) {
                StringBuilder p = new StringBuilder(
                        "SELECT COUNT(*) FROM attendance a " +
                                "LEFT JOIN sessions s ON a.session_id = s.session_id " +
                                "WHERE a.status = 'Present' ");
                List<Object> pp = new ArrayList<>();
                if (filter.from != null) {
                    p.append("AND a.marked_at::date >= ? ");
                    pp.add(Date.valueOf(filter.from));
                }
                if (filter.to != null) {
                    p.append("AND a.marked_at::date <= ? ");
                    pp.add(Date.valueOf(filter.to));
                }
                if (filter.courseId != null) {
                    p.append("AND s.course_id = ? ");
                    pp.add(filter.courseId);
                }
                if (filter.sessionId != null) {
                    p.append("AND a.session_id = ? ");
                    pp.add(filter.sessionId);
                }

                try (PreparedStatement ps = conn.prepareStatement(p.toString())) {
                    for (int i = 0; i < pp.size(); i++)
                        ps.setObject(i + 1, pp.get(i));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next())
                            present = rs.getInt(1);
                    }
                }
            }
        }

        return new DashboardTopCards(students, sessions, present);
    }

    @Override
    public List<String> listCourseLabels() throws SQLException {
        List<String> out = new ArrayList<>();
        out.add("All");
        String sql = "SELECT course_id, course_code, course_name FROM courses ORDER BY course_code";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("course_id");
                String code = rs.getString("course_code");
                String name = rs.getString("course_name");
                out.add(id + " - " + code + " (" + name + ")");
            }
        }
        return out;
    }

    @Override
    public List<String> listSessionLabels(Integer courseId) throws SQLException {
        List<String> out = new ArrayList<>();
        out.add("All");

        StringBuilder sb = new StringBuilder(
                "SELECT session_id, session_date, start_time FROM sessions WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (courseId != null) {
            sb.append("AND course_id = ? ");
            params.add(courseId);
        }
        sb.append("ORDER BY session_date DESC, start_time DESC");

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sb.toString())) {

            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sid = rs.getInt("session_id");
                    Date d = rs.getDate("session_date");
                    Timestamp st = rs.getTimestamp("start_time");

                    String label = String.valueOf(sid);
                    if (d != null) {
                        label += " - " + d.toLocalDate().format(df);
                    }
                    if (st != null) {
                        label += " " + st.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime()
                                .withSecond(0)
                                .withNano(0)
                                .toString();
                    }
                    out.add(label);
                }
            }
        }
        return out;
    }

    @Override
    public String findLatestSessionLabel(Integer courseId) throws SQLException {
        String sql = "SELECT session_id, session_date, start_time FROM sessions "
                + (courseId != null ? "WHERE course_id = ? " : "")
                + "ORDER BY session_date DESC, start_time DESC LIMIT 1";

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            if (courseId != null) {
                ps.setInt(1, courseId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int sid = rs.getInt("session_id");
                    Date d = rs.getDate("session_date");
                    Timestamp st = rs.getTimestamp("start_time");

                    String label = String.valueOf(sid);
                    if (d != null) {
                        label += " - " + d.toLocalDate().format(df);
                    }
                    if (st != null) {
                        label += " " + st.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime()
                                .withSecond(0)
                                .withNano(0)
                                .toString();
                    }
                    return label;
                }
            }
        }

        return null;
    }
}
