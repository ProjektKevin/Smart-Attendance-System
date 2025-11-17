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

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.dto.dashboard.AttendanceRecord;
import com.smartattendance.model.dto.dashboard.DashboardFilter;
import com.smartattendance.model.dto.dashboard.DashboardTopCards;

/**
 * JDBC-based implementation of {@link DashboardRepository}.
 *
 * <p>
 * This class is responsible for:
 * <ul>
 *     <li>Loading raw attendance records for the dashboard, based on a
 *         {@link DashboardFilter}</li>
 *     <li>Computing high-level KPI values for the top dashboard cards</li>
 *     <li>Providing course and session labels for dashboard combo boxes</li>
 *     <li>Finding the latest session label for a given course (or globally)</li>
 * </ul>
 *
 * <p>
 * All database connections are obtained via {@link DatabaseUtil#getConnection()}.
 * SQL is assembled using {@link StringBuilder} plus parameter lists to avoid
 * SQL injection and to keep filtering logic readable.
 * </p>
 *
 * @author Ernest Lun
 */
public class JdbcDashboardRepository implements DashboardRepository {

    /**
     * Loads attendance records that match the given {@link DashboardFilter}.
     *
     * <p>
     * The query joins attendance with users, sessions, and courses to provide
     * all fields needed by {@link AttendanceRecord}. Only basic filtering
     * (date range, course, session) is handled at the SQL level; status
     * filtering is typically handled at the chart/service layer.
     * </p>
     *
     * @param filter dashboard filter (date range, course, session, etc.)
     * @return list of populated {@link AttendanceRecord} instances
     * @throws SQLException if a database error occurs
     */
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

        // Date range filter (on marked_at::date)
        if (filter.from != null) {
            sb.append("AND a.marked_at::date >= ? ");
            params.add(Date.valueOf(filter.from));
        }
        if (filter.to != null) {
            sb.append("AND a.marked_at::date <= ? ");
            params.add(Date.valueOf(filter.to));
        }

        // Course filter
        if (filter.courseId != null) {
            sb.append("AND s.course_id = ? ");
            params.add(filter.courseId);
        }

        // Session filter
        if (filter.sessionId != null) {
            sb.append("AND a.session_id = ? ");
            params.add(filter.sessionId);
        }

        sb.append("ORDER BY a.marked_at DESC");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {

            // Bind all parameters in order
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            // Map each row into an AttendanceRecord DTO
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRecord r = new AttendanceRecord();

                    Timestamp markedTs = rs.getTimestamp("marked_at");
                    if (markedTs != null) {
                        r.markedAt = markedTs.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                    }
                    r.status      = rs.getString("status");
                    r.method      = rs.getString("method");
                    r.confidence  = (Double) rs.getObject("confidence");
                    r.note        = rs.getString("note");
                    r.userId      = (Integer) rs.getObject("user_id");
                    r.username    = rs.getString("username");
                    r.sessionId   = (Integer) rs.getObject("session_id");

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

                    r.courseId   = (Integer) rs.getObject("course_id");
                    r.courseCode = rs.getString("course_code");

                    out.add(r);
                }
            }
        }

        return out;
    }

    /**
     * Computes the high-level KPI values (students, sessions, present) for
     * the dashboard top cards, based on the given filter.
     *
     * <p>
     * This method executes three logical queries:
     * <ul>
     *     <li>Count of distinct students in the filtered attendance records
     *         (fallback: total student count if zero)</li>
     *     <li>Count of sessions in the filtered period (or 1 if a specific
     *         session is selected)</li>
     *     <li>Count of "Present" records (only when {@code includePresent} is true)</li>
     * </ul>
     * </p>
     *
     * @param filter dashboard filter (date range, course, session, status flags)
     * @return a {@link DashboardTopCards} object with aggregate counts
     * @throws SQLException if a database error occurs
     */
    @Override
    public DashboardTopCards computeTopCards(DashboardFilter filter) throws SQLException {
        int students = 0;
        int sessions = 0;
        int present  = 0;

        // Build list of statuses we care about for the "students" query.
        List<String> wantedStatuses = new ArrayList<>();
        if (filter.includePresent) wantedStatuses.add("Present");
        if (filter.includeLate)    wantedStatuses.add("Late");
        if (filter.includeAbsent)  wantedStatuses.add("Absent");
        if (filter.includePending) wantedStatuses.add("Pending");

        try (Connection conn = DatabaseUtil.getConnection()) {
            /* ----- Students (distinct user_id) ----- */
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
                    if (i < wantedStatuses.size() - 1) {
                        stu.append(",");
                    }
                }
                stu.append(") ");
            }

            try (PreparedStatement ps = conn.prepareStatement(stu.toString())) {
                int idx = 1;
                for (Object o : params) {
                    ps.setObject(idx++, o);
                }
                for (String s : wantedStatuses) {
                    ps.setString(idx++, s);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        students = rs.getInt("cnt");
                    }
                }
            }

            // Fallback: if no students found in attendance, use total students from users table.
            if (students == 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM users WHERE role = 'STUDENT'")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            students = rs.getInt(1);
                        }
                    }
                }
            }

            /* ----- Sessions ----- */
            if (filter.sessionId != null) {
                // When a specific session is selected, sessions count is exactly 1.
                sessions = 1;
            } else {
                StringBuilder ssb = new StringBuilder(
                        "SELECT COUNT(*) FROM sessions WHERE 1=1 ");
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
                    for (int i = 0; i < p2.size(); i++) {
                        ps.setObject(i + 1, p2.get(i));
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            sessions = rs.getInt(1);
                        }
                    }
                }
            }

            /* ----- Present count ----- */
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
                    for (int i = 0; i < pp.size(); i++) {
                        ps.setObject(i + 1, pp.get(i));
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            present = rs.getInt(1);
                        }
                    }
                }
            }
        }

        return new DashboardTopCards(students, sessions, present);
    }

    /**
     * Returns course labels for the dashboard course combo box.
     *
     * <p>
     * The first element is always {@code "All"}. Subsequent items follow
     * the format:
     * </p>
     *
     * <pre>
     *     {course_id} - {course_code} ({course_name})
     * </pre>
     *
     * @return list of course labels beginning with {@code "All"}
     * @throws SQLException if a database error occurs
     */
    @Override
    public List<String> listCourseLabels() throws SQLException {
        List<String> out = new ArrayList<>();
        out.add("All");

        String sql = "SELECT course_id, course_code, course_name " +
                     "FROM courses ORDER BY course_code";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id     = rs.getInt("course_id");
                String code = rs.getString("course_code");
                String name = rs.getString("course_name");
                out.add(id + " - " + code + " (" + name + ")");
            }
        }
        return out;
    }

    /**
     * Returns session labels for the dashboard session combo box, optionally
     * filtered by course.
     *
     * <p>
     * The first element is always {@code "All"}. Subsequent items follow
     * the format:
     * </p>
     *
     * <pre>
     *     {session_id} - {session_date} {start_time}
     * </pre>
     *
     * @param courseId course ID to filter sessions by, or {@code null} to
     *                 list sessions across all courses
     * @return list of session labels beginning with {@code "All"}
     * @throws SQLException if a database error occurs
     */
    @Override
    public List<String> listSessionLabels(Integer courseId) throws SQLException {
        List<String> out = new ArrayList<>();
        out.add("All");

        StringBuilder sb = new StringBuilder(
                "SELECT session_id, session_date, start_time " +
                "FROM sessions WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (courseId != null) {
            sb.append("AND course_id = ? ");
            params.add(courseId);
        }
        sb.append("ORDER BY session_date DESC, start_time DESC");

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sid      = rs.getInt("session_id");
                    Date d       = rs.getDate("session_date");
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

    /**
     * Finds the label of the latest session (by date and time), optionally
     * restricted to a particular course.
     *
     * <p>
     * The returned label follows the same format as those from
     * {@link #listSessionLabels(Integer)}, so it can be directly used in
     * the session combo box.
     * </p>
     *
     * @param courseId course ID to filter sessions by, or {@code null} to
     *                 consider all courses
     * @return label of the latest session, or {@code null} if no sessions exist
     * @throws SQLException if a database error occurs
     */
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
                    int sid      = rs.getInt("session_id");
                    Date d       = rs.getDate("session_date");
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
