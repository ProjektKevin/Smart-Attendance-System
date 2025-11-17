package com.smartattendance.controller.student;

import java.time.LocalDate;

/**
 * Immutable view-model representing a single row in the student's
 * detailed attendance table.
 *
 * <p>
 * Each instance contains the attendance date, course, status,
 * capture method, and the timestamp at which the attendance
 * was marked. This is typically used as the backing model for
 * a {@code TableView<StudentAttendanceRow>} in the student
 * attendance screen.
 * </p>
 *
 * @author Ernest Lun
 */
public class StudentAttendanceRow {

    /**
     * Date of the attendance record (session date).
     */
    private final LocalDate date;

    /**
     * Course name or course code associated with this record.
     */
    private final String course;

    /**
     * Attendance status (e.g. "Present", "Late", "Absent", "Pending").
     */
    private final String status;

    /**
     * Method used to mark attendance (e.g. "Face", "Manual").
     */
    private final String method;

    /**
     * Human-readable timestamp describing when the attendance was marked.
     */
    private final String markedAt;

    /**
     * Constructs a new {@code StudentAttendanceRow} with all fields set.
     *
     * @param date     date of the attendance record
     * @param course   course name or code
     * @param status   attendance status string
     * @param method   method used to capture attendance
     * @param markedAt formatted timestamp for when the attendance was recorded
     */
    public StudentAttendanceRow(LocalDate date,
                                String course,
                                String status,
                                String method,
                                String markedAt) {
        this.date = date;
        this.course = course;
        this.status = status;
        this.method = method;
        this.markedAt = markedAt;
    }

    /**
     * Returns the date of this attendance record.
     *
     * @return the session date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the course name or code for this record.
     *
     * @return the course identifier
     */
    public String getCourse() {
        return course;
    }

    /**
     * Returns the attendance status.
     *
     * @return status string (e.g. "Present", "Late")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the method used to mark attendance.
     *
     * @return method label (e.g. "Face", "Manual")
     */
    public String getMethod() {
        return method;
    }

    /**
     * Returns the formatted timestamp at which this attendance was marked.
     *
     * @return human-readable "marked at" string
     */
    public String getMarkedAt() {
        return markedAt;
    }
}
