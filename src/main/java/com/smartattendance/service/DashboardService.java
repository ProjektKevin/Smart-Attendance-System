package com.smartattendance.service;

import java.sql.SQLException;
import java.util.List;

import com.smartattendance.model.dto.dashboard.AttendanceRecord;
import com.smartattendance.model.dto.dashboard.DashboardFilter;
import com.smartattendance.model.dto.dashboard.DashboardTopCards;
import com.smartattendance.repository.DashboardRepository;

/**
 * Service layer for the dashboard module.
 *
 * <p>Responsibilities:
 * <ul>
 *     <li>Provide a clean API for controllers to query dashboard data</li>
 *     <li>Delegate all data access to {@link DashboardRepository}</li>
 *     <li>Keep controllers decoupled from repository and SQL details</li>
 * </ul>
 *
 * <p>This class does not perform heavy business logic; it mainly passes
 * {@link DashboardFilter} to the repository and returns DTOs for the UI.
 */
public class DashboardService {

    /**
     * Repository used to fetch aggregated data for the dashboard.
     */
    private final DashboardRepository repository;

    /**
     * Construct a {@code DashboardService} with the given repository.
     *
     * @param repository repository used for dashboard queries
     */
    public DashboardService(DashboardRepository repository) {
        this.repository = repository;
    }

    /**
     * Load attendance records for the dashboard based on the given filter.
     *
     * <p>Typical consumers: dashboard controllers and chart builders.
     *
     * @param filter filter specifying date range, course, session and status flags
     * @return list of {@link AttendanceRecord} DTOs matching the filter
     * @throws SQLException if an error occurs while querying the database
     */
    public List<AttendanceRecord> loadAttendance(DashboardFilter filter) throws SQLException {
        return repository.findAttendance(filter);
    }

    /**
     * Compute the values for the dashboard top cards (students, sessions, present)
     * based on the current filter.
     *
     * @param filter dashboard filter to restrict which records are counted
     * @return {@link DashboardTopCards} containing the KPI values
     * @throws SQLException if an error occurs while querying the database
     */
    public DashboardTopCards computeTopCards(DashboardFilter filter) throws SQLException {
        return repository.computeTopCards(filter);
    }

    /**
     * Retrieve the list of course labels for use in dashboard dropdowns.
     *
     * @return list of human-readable course labels (e.g. "CS101 - Programming")
     * @throws SQLException if an error occurs while querying the database
     */
    public List<String> listCourses() throws SQLException {
        return repository.listCourseLabels();
    }

    /**
     * Retrieve the list of session labels for a given course, for use
     * in dashboard session dropdowns.
     *
     * @param courseId ID of the course whose sessions should be listed
     * @return list of session labels (e.g. "Session 1 - 2025-01-10")
     * @throws SQLException if an error occurs while querying the database
     */
    public List<String> listSessions(Integer courseId) throws SQLException {
        return repository.listSessionLabels(courseId);
    }

    /**
     * Find a label representing the latest session for the given course.
     * This is typically used to pre-select or highlight the most recent session.
     *
     * @param courseId ID of the course, or {@code null} if using a default/all-course context
     * @return label of the latest session, or {@code null} if no sessions exist
     * @throws SQLException if an error occurs while querying the database
     */
    public String latestSessionLabel(Integer courseId) throws SQLException {
        return repository.findLatestSessionLabel(courseId);
    }
}
