package com.smartattendance.util.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple data transfer object (DTO) representing a single row
 * in an attendance report.
 *
 * <p>This class is typically populated by a repository or service layer
 * and then consumed by exporters (CSV, PDF, XLSX) or the UI layer.
 *
 * <p>Key points:
 * <ul>
 *     <li>Stores both raw data (e.g. {@link #timestamp}) and display-ready values via helpers</li>
 *     <li>Provides a formatted timestamp string for report output</li>
 *     <li>Uses plain getters and setters for easy mapping from JDBC or ORM results</li>
 * </ul>
 * 
 * @author Ernest Lun
 */
public class AttendanceReportRow {

    /**
     * When the attendance was recorded. Can be {@code null} if not set yet.
     */
    private LocalDateTime timestamp;

    /**
     * Identifier of the session this record belongs to
     * (e.g., "CS101-2024-10-01-0900").
     */
    private String sessionId;

    /**
     * Course code associated with the session (e.g., "CS101").
     */
    private String courseCode;

    /**
     * Unique identifier of the student (e.g., matric number or student ID).
     */
    private String studentId;

    /**
     * Full display name of the student.
     */
    private String studentName;

    /**
     * Attendance status (e.g., "Present", "Late", "Absent", "Pending").
     */
    private String status;

    /**
     * Method used to mark attendance (e.g., "Manual", "Face Recognition").
     */
    private String method;

    /**
     * Confidence value for the method, typically used for face recognition.
     * Stored as a string to allow flexible formatting (e.g., "97%", "0.97").
     */
    private String confidence;

    /**
     * Optional free-form note associated with this record
     * (e.g., "Approved by lecturer", "Manual override").
     */
    private String note;

    /**
     * Setter used by the repository or service layer to assign the timestamp.
     *
     * @param timestamp the date-time when attendance was recorded; may be {@code null}
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Primary getter for the raw {@link LocalDateTime} value.
     * This is useful for logic that needs to sort, filter, or compare by time.
     *
     * @return the underlying timestamp, or {@code null} if not set
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Convenience method for returning the timestamp in a standard
     * string format suitable for CSV, PDF, or log output.
     *
     * <p>Format: {@code "yyyy-MM-dd HH:mm:ss"} (e.g., {@code "2025-11-13 09:30:00"}).
     *
     * @return a formatted timestamp string, or an empty string if {@code timestamp} is {@code null}
     */
    public String getTimestampFormatted() {
        if (timestamp == null) return "";
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ===== other fields =====

    /**
     * @return the session ID associated with this report row
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId the session ID to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the course code for this record
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * @param courseCode the course code to set
     */
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    /**
     * @return the student ID for this record
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * @param studentId the student ID to set
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    /**
     * @return the student's display name
     */
    public String getStudentName() {
        return studentName;
    }

    /**
     * @param studentName the student's display name to set
     */
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    /**
     * @return the attendance status (e.g., "Present", "Late", "Absent", "Pending")
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the attendance status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the method used to mark attendance
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method the method used to mark attendance (e.g., "Manual", "Face Recognition")
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return the confidence string (e.g., "97%", "0.97") or {@code null} if not set
     */
    public String getConfidence() {
        return confidence;
    }

    /**
     * @param confidence the confidence value to set, formatted as a string
     */
    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    /**
     * @return an optional note for this record
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note a free-form note to associate with this record
     */
    public void setNote(String note) {
        this.note = note;
    }
}
