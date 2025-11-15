package com.smartattendance.model.dto.dashboard;

import java.time.LocalDate;

/**
 * Data transfer object (DTO) representing a single attendance row
 * for the student-facing dashboard / attendance history view.
 *
 * <p>This class mirrors what {@code AttendanceRecordRepository} constructs in
 * {@code findByStudentId(...)}:
 * <pre>
 *     new AttendanceRow(sessionDate, startTime, endTime, courseCode, courseName, status)
 * </pre>
 *
 * <p>Purpose:
 * <ul>
 *     <li>Decouple the controller and UI layer from the database/entity model</li>
 *     <li>Provide a simple, read-friendly object for table views and summaries</li>
 * </ul>
 */
public class AttendanceRow {

    /**
     * Date of the session for this attendance entry.
     */
    private LocalDate sessionDate;

    /**
     * Session start time as a formatted string
     * (e.g. "09:00", "14:30"). Stored as a string because
     * the dashboard usually displays it directly.
     */
    private String startTime;

    /**
     * Session end time as a formatted string.
     */
    private String endTime;

    /**
     * Course code associated with this session (e.g. "CS101").
     */
    private String courseCode;

    /**
     * Human-readable course name (e.g. "Programming Fundamentals").
     */
    private String courseName;

    /**
     * Attendance status for this session (e.g. "Present", "Late", "Absent", "Pending").
     */
    private String status;

    /**
     * Construct a fully-initialised attendance row.
     *
     * @param sessionDate date of the session
     * @param startTime   session start time (formatted string)
     * @param endTime     session end time (formatted string)
     * @param courseCode  course code
     * @param courseName  full course name
     * @param status      attendance status
     */
    public AttendanceRow(LocalDate sessionDate,
                         String startTime,
                         String endTime,
                         String courseCode,
                         String courseName,
                         String status) {
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.status = status;
    }

    /**
     * @return date of the session
     */
    public LocalDate getSessionDate() {
        return sessionDate;
    }

    /**
     * @param sessionDate date of the session to set
     */
    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    /**
     * @return start time (formatted string)
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @param startTime start time (formatted string) to set
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * @return end time (formatted string)
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * @param endTime end time (formatted string) to set
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * @return course code for this session
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * @param courseCode course code to set
     */
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    /**
     * @return human-readable course name
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * @param courseName human-readable course name to set
     */
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    /**
     * @return attendance status (e.g. "Present", "Late", "Absent", "Pending")
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status attendance status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * String representation useful for logging and debugging.
     */
    @Override
    public String toString() {
        return "AttendanceRow{" +
                "sessionDate=" + sessionDate +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", courseName='" + courseName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
