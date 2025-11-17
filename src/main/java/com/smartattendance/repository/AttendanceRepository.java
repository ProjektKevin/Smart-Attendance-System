package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.service.AttendanceReportService;
import com.smartattendance.util.report.AttendanceReportRow;

/**
 * Repository responsible for querying attendance- and session-related data
 * used by the reporting module.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Provide option lists for sessions and courses (for dropdowns)</li>
 *     <li>Fetch information about the latest session</li>
 *     <li>Query filtered attendance records for report generation</li>
 * </ul>
 *
 * <p>
 * All database access goes through {@link DatabaseUtil#getConnection()}, and
 * SQL is constructed in this class. Higher-level filtering and report
 * shaping is handled by {@link com.smartattendance.service.AttendanceReportService}
 * and {@link com.smartattendance.util.report.AttendanceReportRow}.
 * </p>
 *
 * @author Ernest Lun
 */
public class AttendanceRepository {

    /* ===== combos ===== */

    /**
     * Fetches a list of session labels for use in "Session" combo boxes.
     *
     * <p>
     * The first element is always {@code "All"}. Subsequent elements have
     * the format:
     * </p>
     *
     * <pre>
     *     {session_id} - {session_date} {start_time} | {course_code} ({course_name})
     * </pre>
     *
     * <p>
     * This allows the caller to both display friendly text and later parse
     * the numeric ID using the {@link #parseIdFromDisplay(String)} helper.
     * </p>
     *
     * @return list of session option strings, beginning with {@code "All"}
     */
    public List<String> fetchSessionOptions() {
        List<String> out = new ArrayList<>();
        out.add("All");

        String sql = "SELECT s.session_id, s.session_date, s.start_time, c.course_code, c.course_name " +
                "FROM sessions s " +
                "LEFT JOIN courses c ON s.course_id = c.course_id " +
                "ORDER BY s.session_date DESC, s.start_time DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("session_id");

                Date d = rs.getDate("session_date");
                LocalDate date = d != null ? d.toLocalDate() : null;

                Timestamp ts = rs.getTimestamp("start_time");
                LocalTime time = ts != null
                        ? ts.toLocalDateTime().toLocalTime()
                        : LocalTime.MIDNIGHT;

                String code = rs.getString("course_code");
                String name = rs.getString("course_name");

                String label = id + " - " +
                        (date != null ? date.toString() : "") + " " + time +
                        " | " + (code != null ? code : "") +
                        (name != null ? " (" + name + ")" : "");

                out.add(label);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Fetches a list of course labels for use in "Course" combo boxes.
     *
     * <p>
     * The first element is always {@code "All"}. Subsequent elements have
     * the format:
     * </p>
     *
     * <pre>
     *     {course_id} - {course_code} ({course_name})
     * </pre>
     *
     * <p>
     * Again, this format allows the UI to display friendly text while still
     * being able to recover the numeric ID using {@link #parseIdFromDisplay(String)}.
     * </p>
     *
     * @return list of course option strings, beginning with {@code "All"}
     */
    public List<String> fetchCourseOptions() {
        List<String> out = new ArrayList<>();
        out.add("All");

        String sql = "SELECT course_id, course_code, course_name " +
                     "FROM courses ORDER BY course_code";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("course_id");
                String code = rs.getString("course_code");
                String name = rs.getString("course_name");
                out.add(id + " - " + code + " (" + name + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Fetches information about the latest session (by date and start time).
     *
     * <p>
     * The query returns at most one record, which is mapped into
     * {@link AttendanceReportService.LatestSessionInfo}. Both a session
     * display label and a course display label are constructed using the
     * same conventions as {@link #fetchSessionOptions()} and
     * {@link #fetchCourseOptions()}.
     * </p>
     *
     * @return a populated {@code LatestSessionInfo}, or {@code null} if no sessions exist
     */
    public AttendanceReportService.LatestSessionInfo fetchLatestSession() {
        String sql = "SELECT s.session_id, s.session_date, s.start_time, " +
                "s.course_id, c.course_code, c.course_name " +
                "FROM sessions s " +
                "LEFT JOIN courses c ON s.course_id = c.course_id " +
                "ORDER BY s.session_date DESC, s.start_time DESC " +
                "LIMIT 1";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                AttendanceReportService.LatestSessionInfo info =
                        new AttendanceReportService.LatestSessionInfo();

                int sessionId = rs.getInt("session_id");

                Date d = rs.getDate("session_date");
                LocalDate date = d != null ? d.toLocalDate() : LocalDate.now();

                Timestamp ts = rs.getTimestamp("start_time");
                LocalTime time = ts != null
                        ? ts.toLocalDateTime().toLocalTime()
                        : LocalTime.MIDNIGHT;

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* ===== data for report ===== */

    /**
     * Finds attendance records that match the given filter parameters and maps
     * them into {@link AttendanceReportRow} instances.
     *
     * <p>
     * Supported filters:
     * <ul>
     *     <li>Date range (from/to, applied on {@code marked_at})</li>
     *     <li>Specific session (parsed from session display string)</li>
     *     <li>Specific course (parsed from course display string)</li>
     *     <li>Status and method (exact match unless "All")</li>
     *     <li>Confidence expressions like {@code ">=0.8"} or {@code "<0.5"}</li>
     * </ul>
     * </p>
     *
     * @param from           start date of the filter (inclusive), or {@code null} for no lower bound
     * @param to             end date of the filter (inclusive), or {@code null} for no upper bound
     * @param sessionDisplay session combo label (e.g. {@code "1 - 2024-01-01 09:00 | DAAA101"})
     * @param courseDisplay  course combo label (e.g. {@code "2 - DAAA101 (Course Name)"})
     * @param status         attendance status filter, or {@code "All"} / {@code null} for no filter
     * @param method         method filter, or {@code "All"} / {@code null} for no filter
     * @param confidenceExpr confidence expression like {@code ">=0.8"}, {@code "<0.5"}, or {@code "All"}
     * @return list of {@link AttendanceReportRow} matching the filters; never {@code null}
     */
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
                        "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // Date filters
        if (from != null) {
            sb.append("AND a.marked_at >= ? ");
            params.add(Timestamp.valueOf(from.atStartOfDay()));
        }
        if (to != null) {
            sb.append("AND a.marked_at <= ? ");
            params.add(Timestamp.valueOf(to.atTime(23, 59, 59)));
        }

        // Session filter
        Integer sessionId = parseIdFromDisplay(sessionDisplay);
        if (sessionId != null) {
            sb.append("AND a.session_id = ? ");
            params.add(sessionId);
        }

        // Course filter
        Integer courseId = parseIdFromDisplay(courseDisplay);
        if (courseId != null) {
            sb.append("AND c.course_id = ? ");
            params.add(courseId);
        }

        // Status filter
        if (status != null && !"All".equalsIgnoreCase(status)) {
            sb.append("AND a.status = ? ");
            params.add(status);
        }

        // Method filter
        if (method != null && !"All".equalsIgnoreCase(method)) {
            sb.append("AND a.method = ? ");
            params.add(method);
        }

        // Confidence filter (simple expressions: ">=x" or "<x")
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

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {

            // Bind all collected parameters in order.
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

                    r.setSessionId(String.valueOf(rs.getInt("session_id")));
                    r.setCourseCode(rs.getString("course_code"));
                    r.setStudentId(String.valueOf(rs.getInt("user_id")));
                    r.setStudentName(rs.getString("username"));
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

        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }

        return out;
    }

    /**
     * Parses a numeric ID from a display string of the form:
     *
     * <pre>
     *     {id} - {rest of label}
     * </pre>
     *
     * <p>
     * If the input is {@code null}, blank, or equal to {@code "All"}, this
     * method returns {@code null}. If the ID prefix cannot be parsed, it
     * also returns {@code null}.
     * </p>
     *
     * @param val display label containing an ID prefix
     * @return parsed integer ID, or {@code null} if not applicable / invalid
     */
    private Integer parseIdFromDisplay(String val) {
        if (val == null || val.isBlank() || "All".equals(val)) {
            return null;
        }
        String[] parts = val.split(" - ", 2);
        try {
            return Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
