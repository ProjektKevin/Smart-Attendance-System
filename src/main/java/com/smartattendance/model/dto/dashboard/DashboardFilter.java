package com.smartattendance.model.dto.dashboard;

import java.time.LocalDate;

/**
 * Simple filter object used by the dashboard to query and aggregate
 * attendance data.
 *
 * <p>Typical usage:
 * <ul>
 *     <li>Controllers populate this before calling dashboard services/repositories</li>
 *     <li>Dashboard services read these fields to limit results by date, course,
 *         session, and which statuses should be included in charts/tables</li>
 * </ul>
 *
 * <p>This is intentionally a very lightweight DTO with public fields so it is
 * easy to construct and pass around in the dashboard layer.
 *
 * @author Ernest Lun
 */

public class DashboardFilter {

    /**
     * Start of the date range (inclusive) for filtering attendance records.
     * May be {@code null} to indicate "no lower bound".
     */
    public LocalDate from;

    /**
     * End of the date range (inclusive) for filtering attendance records.
     * May be {@code null} to indicate "no upper bound".
     */
    public LocalDate to;

    /**
     * Optional course ID to restrict results to a single course.
     * If {@code null}, records from all courses are included.
     */
    public Integer courseId;

    /**
     * Optional session ID to restrict results to a single session.
     * If {@code null}, records from all sessions are included (subject to other filters).
     */
    public Integer sessionId;

    /**
     * Whether "Present" records should be included in the results and charts.
     * Defaults to {@code true}.
     */
    public boolean includePresent = true;

    /**
     * Whether "Late" records should be included in the results and charts.
     * Defaults to {@code true}.
     */
    public boolean includeLate    = true;

    /**
     * Whether "Absent" records should be included in the results and charts.
     * Defaults to {@code true}.
     */
    public boolean includeAbsent  = true;

    /**
     * Whether "Pending" records should be included in the results and charts.
     * Defaults to {@code true}.
     */
    public boolean includePending = true;
}
