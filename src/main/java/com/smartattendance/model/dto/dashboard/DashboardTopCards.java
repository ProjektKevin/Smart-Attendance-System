package com.smartattendance.model.dto.dashboard;

/**
 * Simple immutable DTO representing the high-level KPI values
 * shown in the top cards of the dashboard.
 *
 * <p>Typical usage:
 * <ul>
 *     <li>Dashboard service computes these aggregate counts</li>
 *     <li>Controller binds them to UI "summary cards" (e.g. total students, sessions)</li>
 * </ul>
 *
 * @author Ernest Lun
 */

public class DashboardTopCards {

    /**
     * Total number of distinct students considered in the current filter range.
     */
    private final int students;

    /**
     * Total number of sessions considered in the current filter range.
     */
    private final int sessions;

    /**
     * Total number of "Present" attendance records (or similar aggregate),
     * used to show how many attendances were successfully recorded.
     */
    private final int present;

    /**
     * Create a new {@code DashboardTopCards} instance with all KPI values.
     *
     * @param students total number of students
     * @param sessions total number of sessions
     * @param present  total number of present attendances
     */
    public DashboardTopCards(int students, int sessions, int present) {
        this.students = students;
        this.sessions = sessions;
        this.present = present;
    }

    /**
     * @return total number of students represented in the dashboard summary
     */
    public int getStudents() {
        return students;
    }

    /**
     * @return total number of sessions represented in the dashboard summary
     */
    public int getSessions() {
        return sessions;
    }

    /**
     * @return total number of present attendances (or equivalent metric)
     */
    public int getPresent() {
        return present;
    }
}
