package com.smartattendance.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.smartattendance.controller.student.StudentAttendanceRow;
import com.smartattendance.controller.student.StudentCourseSummary;
import com.smartattendance.model.dto.dashboard.AttendanceRecord;
import com.smartattendance.model.dto.dashboard.DashboardFilter;
import com.smartattendance.repository.DashboardRepository;
import com.smartattendance.repository.JdbcDashboardRepository;
import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * Service layer for the student-facing attendance history view.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Load raw {@link AttendanceRecord} instances via the {@link DashboardRepository}</li>
 *     <li>Map records into {@link StudentAttendanceRow} for the UI table</li>
 *     <li>Apply course/date filters and compute per-course summaries
 *         ({@link StudentCourseSummary})</li>
 * </ul>
 *
 * <p>
 * This service keeps the controller free from repository and transformation logic,
 * providing ready-to-bind DTOs for JavaFX views.
 * </p>
 *
 * @author Ernest Lun
 */
public class StudentAttendanceService {

    /**
     * Repository used to load attendance records for the student dashboard.
     * Currently backed by {@link JdbcDashboardRepository}.
     */
    private final DashboardRepository repo = new JdbcDashboardRepository();

    /**
     * Application-wide logger for recording errors and debug information.
     */
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    /**
     * Loads all attendance rows for a given student and maps them into
     * {@link StudentAttendanceRow} instances suitable for the student
     * attendance table.
     *
     * <p>
     * Implementation details:
     * <ul>
     *     <li>Creates a broad {@link DashboardFilter} from 1 Jan 2020 up to
     *         (today + 1 day) to capture all historical records</li>
     *     <li>Delegates to {@link DashboardRepository#findAttendance(DashboardFilter)}</li>
     *     <li>Filters by {@code studentId}</li>
     *     <li>Derives display fields (date, course label, status, method, markedAt string)</li>
     *     <li>Applies sensible fallbacks such as "Unknown" or "Pending" when data is missing</li>
     * </ul>
     * </p>
     *
     * @param studentId the ID of the student whose attendance history is requested
     * @return list of {@link StudentAttendanceRow} for that student; never {@code null}
     */
    public List<StudentAttendanceRow> loadRowsForStudent(int studentId) {
        DashboardFilter filter = new DashboardFilter();
        filter.from = LocalDate.of(2020, 1, 1);
        filter.to   = LocalDate.now().plusDays(1);

        List<AttendanceRecord> all;
        try {
            all = repo.findAttendance(filter);
        } catch (Exception e) {
            appLogger.error("[StudentAttendanceService] fetch failed", e);
            return List.of();
        }

        List<StudentAttendanceRow> rows = new ArrayList<>();
        for (AttendanceRecord rec : all) {
            if (rec.userId == null || rec.userId != studentId) {
                continue;
            }

            // Prefer session date, fall back to markedAt, then today as last resort
            LocalDate date = rec.sessionDate != null
                    ? rec.sessionDate
                    : (rec.markedAt != null ? rec.markedAt.toLocalDate() : LocalDate.now());

            // Course label: code if available, otherwise "Course {id}" or "Unknown"
            String course = (rec.courseCode != null && !rec.courseCode.isBlank())
                    ? rec.courseCode
                    : (rec.courseId != null ? "Course " + rec.courseId : "Unknown");

            // Status label: capitalized, default "Pending"
            String status = (rec.status != null && !rec.status.isBlank())
                    ? capitalize(rec.status)
                    : "Pending";

            // Method label: capitalized, default "Unknown"
            String method = (rec.method != null && !rec.method.isBlank())
                    ? capitalize(rec.method)
                    : "Unknown";

            // Marked-at string in a user-friendly form (ISO, with space instead of 'T')
            String markedAtStr = (rec.markedAt != null)
                    ? rec.markedAt.toString().replace('T', ' ')
                    : "";

            rows.add(new StudentAttendanceRow(date, course, status, method, markedAtStr));
        }

        return rows;
    }

    /**
     * Applies course and date filters to a master list of student attendance rows
     * and computes per-course attendance summaries.
     *
     * <p>
     * Filtering rules:
     * <ul>
     *     <li>If {@code selectedCourse} is {@code null} or equals {@code "All courses"},
     *         all courses are included; otherwise only exact matches are kept.</li>
     *     <li>{@code from}: inclusive lower bound on date (if non-null).</li>
     *     <li>{@code to}: inclusive upper bound on date (if non-null).</li>
     *     <li>Results are sorted by date in descending order.</li>
     * </ul>
     *
     * <p>
     * Summary rules:
     * <ul>
     *     <li>Rows are grouped by course label.</li>
     *     <li>"Attended" is counted as rows where status equals "present"
     *         (case-insensitive).</li>
     *     <li>Ratio is {@code attended / total}, or {@code NaN} if total is 0.</li>
     * </ul>
     * </p>
     *
     * @param master         full list of attendance rows for a student
     * @param selectedCourse currently selected course filter or {@code "All courses"}
     * @param from           inclusive start date filter; may be {@code null}
     * @param to             inclusive end date filter; may be {@code null}
     * @return {@link FilterResult} containing filtered rows and per-course summaries
     */
    public FilterResult applyFilters(
            List<StudentAttendanceRow> master,
            String selectedCourse,
            LocalDate from,
            LocalDate to) {

        List<StudentAttendanceRow> filtered = master.stream()
                .filter(r -> selectedCourse == null
                        || "All courses".equals(selectedCourse)
                        || r.getCourse().equals(selectedCourse))
                .filter(r -> from == null || !r.getDate().isBefore(from))
                .filter(r -> to == null || !r.getDate().isAfter(to))
                .sorted(Comparator.comparing(StudentAttendanceRow::getDate).reversed())
                .toList();

        // Group rows by course
        Map<String, List<StudentAttendanceRow>> byCourse = new TreeMap<>();
        for (StudentAttendanceRow r : filtered) {
            byCourse.computeIfAbsent(r.getCourse(), k -> new ArrayList<>()).add(r);
        }

        // Build summaries for each course
        List<StudentCourseSummary> summaries = new ArrayList<>();
        byCourse.forEach((course, list) -> {
            long attended = list.stream()
                    .filter(r -> "present".equalsIgnoreCase(r.getStatus()))
                    .count();
            int total = list.size();
            double ratio = total == 0 ? Double.NaN : (attended * 1.0 / total);
            summaries.add(new StudentCourseSummary(course, (int) attended, total, ratio));
        });

        return new FilterResult(filtered, summaries);
    }

    /**
     * Result object bundling filtered rows and computed summaries for
     * the student attendance view.
     */
    public static class FilterResult {
        private final List<StudentAttendanceRow> rows;
        private final List<StudentCourseSummary> summaries;

        /**
         * Creates a new immutable filter result.
         *
         * @param rows      filtered student attendance rows
         * @param summaries per-course attendance summaries
         */
        public FilterResult(List<StudentAttendanceRow> rows, List<StudentCourseSummary> summaries) {
            this.rows = rows;
            this.summaries = summaries;
        }

        /**
         * @return filtered attendance rows for the table
         */
        public List<StudentAttendanceRow> getRows() {
            return rows;
        }

        /**
         * @return per-course summaries for the summary table / chart
         */
        public List<StudentCourseSummary> getSummaries() {
            return summaries;
        }
    }

    /**
     * Utility method to normalize a string to "Sentence case" (lowercase
     * then capitalise the first character).
     *
     * @param s input string (e.g. "present", "PRESENT")
     * @return capitalized string (e.g. "Present"), or original if blank/null
     */
    private static String capitalize(String s) {
        if (s == null || s.isBlank()) {
            return s;
        }
        s = s.toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
