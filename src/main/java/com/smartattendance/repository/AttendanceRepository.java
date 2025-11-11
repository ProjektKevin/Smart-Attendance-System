package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.smartattendance.service.AttendanceReportService;
import com.smartattendance.util.report.AttendanceReportRow;

public class AttendanceRepository {

    private static final String DB_URL  = System.getenv().getOrDefault(
            "DATABASE_URL",
            "jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?user=postgres.muholwznrkonrcjesdij"
    );
    private static final String DB_PASS = System.getenv().getOrDefault(
            "DATABASE_PASSWORD",
            "TheDawn5"
    );

    private Connection openConnection() throws Exception {
        Properties props = new Properties();
        props.setProperty("password", DB_PASS);
        return DriverManager.getConnection(DB_URL, props);
    }

    /* ===== combos ===== */

    public List<String> fetchSessionOptions() {
        List<String> out = new ArrayList<>();
        out.add("All");
        String sql =
                "SELECT s.session_id, s.session_date, s.start_time, c.course_code, c.course_name " +
                "FROM sessions s " +
                "LEFT JOIN courses c ON s.course_id = c.course_id " +
                "ORDER BY s.session_date DESC, s.start_time DESC";
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("session_id");
                Date d = rs.getDate("session_date");
                LocalDate date = d != null ? d.toLocalDate() : null;

                // start_time in your schema is TIMESTAMP, but we only need time-of-day for display
                Timestamp ts = rs.getTimestamp("start_time");
                LocalTime time = ts != null ? ts.toLocalDateTime().toLocalTime() : LocalTime.MIDNIGHT;

                String code = rs.getString("course_code");
                String name = rs.getString("course_name");
                String label = id + " - " + (date != null ? date.toString() : "") + " " + time +
                        " | " + (code != null ? code : "") +
                        (name != null ? " (" + name + ")" : "");
                out.add(label);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public List<String> fetchCourseOptions() {
        List<String> out = new ArrayList<>();
        out.add("All");
        String sql = "SELECT course_id, course_code, course_name FROM courses ORDER BY course_code";
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("course_id");
                String code = rs.getString("course_code");
                String name = rs.getString("course_name");
                out.add(id + " - " + code + " (" + name + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public AttendanceReportService.LatestSessionInfo fetchLatestSession() {
        String sql =
                "SELECT s.session_id, s.session_date, s.start_time, s.course_id, c.course_code, c.course_name " +
                "FROM sessions s " +
                "LEFT JOIN courses c ON s.course_id = c.course_id " +
                "ORDER BY s.session_date DESC, s.start_time DESC " +
                "LIMIT 1";
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                AttendanceReportService.LatestSessionInfo info = new AttendanceReportService.LatestSessionInfo();

                int sessionId = rs.getInt("session_id");
                Date d = rs.getDate("session_date");
                LocalDate date = d != null ? d.toLocalDate() : LocalDate.now();

                Timestamp ts = rs.getTimestamp("start_time");
                LocalTime time = ts != null ? ts.toLocalDateTime().toLocalTime() : LocalTime.MIDNIGHT;

                String code = rs.getString("course_code");
                String name = rs.getString("course_name");
                int courseId = rs.getInt("course_id");

                info.date = date;
                info.display = sessionId + " - " + date + " " + time +
                        " | " + (code != null ? code : "") +
                        (name != null ? " (" + name + ")" : "");
                info.courseDisplay = courseId + " - " + (code != null ? code : "") +
                        (name != null ? " (" + name + ")" : "");

                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* ===== data for report ===== */

    public List<AttendanceReportRow> findAttendance(LocalDate from,
                                                    LocalDate to,
                                                    String sessionDisplay,
                                                    String courseDisplay,
                                                    String status,
                                                    String method,
                                                    String confidenceExpr) {
        List<AttendanceReportRow> out = new ArrayList<>();

        StringBuilder sb = new StringBuilder(
                "SELECT " +
                        "a.marked_at, " +
                        "a.session_id, " +
                        "a.user_id, " +
                        "u.username, " +
                        "a.status, " +
                        "a.method, " +
                        "a.note, " +
                        "a.confidence, " +
                        "c.course_code, " +
                        "c.course_id " +
                "FROM attendance a " +
                "JOIN sessions s ON a.session_id = s.session_id " +
                "JOIN courses  c ON s.course_id = c.course_id " +
                "LEFT JOIN users u ON a.user_id = u.user_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // date range on the timestamp in attendance
        if (from != null) {
            sb.append("AND a.marked_at >= ? ");
            params.add(Timestamp.valueOf(from.atStartOfDay()));
        }
        if (to != null) {
            sb.append("AND a.marked_at <= ? ");
            params.add(Timestamp.valueOf(to.atTime(23, 59, 59)));
        }

        // session filter (combo text is "id - ...")
        Integer sessionId = parseIdFromDisplay(sessionDisplay);
        if (sessionId != null) {
            sb.append("AND a.session_id = ? ");
            params.add(sessionId);
        }

        // course filter (combo text is "id - ...")
        Integer courseId = parseIdFromDisplay(courseDisplay);
        if (courseId != null) {
            sb.append("AND c.course_id = ? ");
            params.add(courseId);
        }

        // status
        if (status != null && !"All".equalsIgnoreCase(status)) {
            sb.append("AND a.status = ? ");
            params.add(status);
        }

        // method
        if (method != null && !"All".equalsIgnoreCase(method)) {
            sb.append("AND a.method = ? ");
            params.add(method);
        }

        // confidence
        if (confidenceExpr != null && !"All".equalsIgnoreCase(confidenceExpr)) {
            if (confidenceExpr.startsWith(">=")) {
                double min = Double.parseDouble(confidenceExpr.substring(2).trim());
                sb.append("AND a.confidence >= ? ");
                params.add(min);
            } else if (confidenceExpr.startsWith("<")) {
                double max = Double.parseDouble(confidenceExpr.substring(1).trim());
                sb.append("AND a.confidence < ? ");
                params.add(max);
            }
        }

        sb.append("ORDER BY a.marked_at DESC");

        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceReportRow r = new AttendanceReportRow();

                    Timestamp ts = rs.getTimestamp("marked_at");
                    if (ts != null) {
                        r.setTimestamp(ts.toLocalDateTime());
                    }

                    // session id
                    r.setSessionId(String.valueOf(rs.getInt("session_id")));

                    // course code from courses table
                    r.setCourseCode(rs.getString("course_code"));

                    // student
                    r.setStudentId(String.valueOf(rs.getInt("user_id")));
                    r.setStudentName(rs.getString("username")); // you could join profile if you want full name

                    r.setStatus(rs.getString("status"));
                    r.setMethod(rs.getString("method"));
                    r.setNote(rs.getString("note"));

                    Object conf = rs.getObject("confidence");
                    if (conf != null) {
                        r.setConfidence(String.valueOf(conf));
                    }

                    out.add(r);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }

        return out;
    }

    private Integer parseIdFromDisplay(String val) {
        if (val == null || val.isBlank() || "All".equals(val)) return null;
        String[] parts = val.split(" - ", 2);
        try {
            return Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
