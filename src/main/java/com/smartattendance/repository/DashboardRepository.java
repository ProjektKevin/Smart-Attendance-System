package com.smartattendance.repository;

import java.sql.SQLException;
import java.util.List;

import com.smartattendance.model.dto.dashboard.AttendanceRecord;
import com.smartattendance.model.dto.dashboard.DashboardFilter;
import com.smartattendance.model.dto.dashboard.DashboardTopCards;

/**
 * Repository abstraction for dashboard-related data access.
 *
 * <p>
 * This interface defines the contract that the dashboard layer relies on
 * for querying attendance data, computing high-level KPIs, and loading
 * combo-box labels for courses and sessions.
 * </p>
 *
 * <p>
 * Implementations (e.g. {@code JdbcDashboardRepository}) are responsible
 * for performing the actual SQL queries and mapping database rows into the
 * corresponding DTOs in the {@code model.dto.dashboard} package.
 * </p>
 *
 * @author Ernest Lun
 */
public interface DashboardRepository {

    /**
     * Finds attendance records that match the given dashboard filter.
     *
     * <p>
     * Typical usage:
     * <ul>
     *     <li>Controller builds a {@link DashboardFilter} based on UI inputs</li>
     *     <li>Service calls this method to retrieve the matching records</li>
     *     <li>Returned records are used to populate charts and tables</li>
     * </ul>
     * </p>
     *
     * @param filter filter object describing date range, course, session,
     *               and included statuses
     * @return list of matching {@link AttendanceRecord} instances
     * @throws SQLException if a database access error occurs
     */
    List<AttendanceRecord> findAttendance(DashboardFilter filter) throws SQLException;

    /**
     * Computes high-level KPI values (top cards) for the given filter.
     *
     * <p>
     * Examples of top-card values include:
     * <ul>
     *     <li>Total distinct students</li>
     *     <li>Total sessions</li>
     *     <li>Number of present students in the selected period</li>
     * </ul>
     * </p>
     *
     * @param filter filter object describing date range, course, and session
     * @return a {@link DashboardTopCards} DTO with aggregated counts
     * @throws SQLException if a database access error occurs
     */
    DashboardTopCards computeTopCards(DashboardFilter filter) throws SQLException;

    /**
     * Returns all course labels to be shown in the dashboard course combo box.
     *
     * <p>
     * Labels are typically in a format such as:
     * <pre>
     *     {course_id} - {course_code} ({course_name})
     * </pre>
     * This allows the UI to display human-readable text while still being able
     * to parse the numeric ID when needed.
     * </p>
     *
     * @return list of course labels
     * @throws SQLException if a database access error occurs
     */
    List<String> listCourseLabels() throws SQLException;

    /**
     * Returns all session labels for the given course, or all sessions when
     * {@code courseId} is {@code null}.
     *
     * <p>
     * Labels are typically in a format such as:
     * <pre>
     *     {session_id} - {session_date} {start_time} | {course_code} ({course_name})
     * </pre>
     * </p>
     *
     * @param courseId course identifier to filter sessions by, or {@code null}
     *                 to return sessions across all courses
     * @return list of session labels
     * @throws SQLException if a database access error occurs
     */
    List<String> listSessionLabels(Integer courseId) throws SQLException;

    /**
     * Finds the label of the most recent session (by date/time) for the given course.
     *
     * <p>
     * The label follows the same format as the values returned by
     * {@link #listSessionLabels(Integer)}, so it can be directly used to
     * select the latest session in a combo box.
     * </p>
     *
     * @param courseId course identifier to filter sessions by, or {@code null}
     *                 to consider sessions across all courses
     * @return label of the latest session, or {@code null} if no sessions exist
     * @throws SQLException if a database access error occurs
     */
    String findLatestSessionLabel(Integer courseId) throws SQLException;
}
