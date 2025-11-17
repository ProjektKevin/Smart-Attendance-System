package com.smartattendance.model.dto.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lightweight data transfer object (DTO) representing a single
 * attendance record as used by the dashboard layer.
 *
 * <p>This class is intentionally simple and uses public fields so that:
 * <ul>
 *     <li>Repository/service code can populate it quickly</li>
 *     <li>Dashboard/chart utilities (e.g. {@code DashboardCharts}) can
 *         read values without boilerplate getters</li>
 * </ul>
 *
 * <p>It is separate from the persistence/entity model to avoid coupling the
 * dashboard views directly to the database schema.
 *
 * @author Ernest Lun
 */
public class AttendanceRecord {

    /**
     * Date and time when the attendance was marked
     * (e.g. when the student scanned / was recognised).
     */
    public LocalDateTime markedAt;

    /**
     * Attendance status, such as "Present", "Late", "Absent", or "Pending".
     */
    public String status;

    /**
     * Method used to record attendance, e.g. "Manual" or "Face Recognition".
     */
    public String method;

    /**
     * Confidence value for the attendance method (typically for face recognition).
     * May be {@code null} if not applicable.
     */
    public Double confidence;

    /**
     * Optional free-form note associated with this record
     * (e.g. "Approved by lecturer", "Manual override").
     */
    public String note;

    /**
     * Internal user ID of the student (database identifier).
     */
    public Integer userId;

    /**
     * Username or login identifier of the student.
     */
    public String username;

    /**
     * Internal ID of the session (database identifier).
     */
    public Integer sessionId;

    /**
     * Date on which the session takes place.
     */
    public LocalDate sessionDate;

    /**
     * Start date-time of the session (used for arrival calculations, etc.).
     */
    public LocalDateTime sessionStart;

    /**
     * Internal ID of the course (database identifier).
     */
    public Integer courseId;

    /**
     * Course code associated with this attendance record (e.g. "CS101").
     */
    public String courseCode;
}
