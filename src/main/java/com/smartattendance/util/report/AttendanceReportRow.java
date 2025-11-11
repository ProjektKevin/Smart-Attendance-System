package com.smartattendance.util.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AttendanceReportRow {

    private LocalDateTime timestamp;
    private String sessionId;
    private String courseCode;
    private String studentId;
    private String studentName;
    private String status;
    private String method;
    private String confidence;
    private String note;

    // repo calls this
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // main getter for code that wants the actual date-time
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // convenient string (for CSV / PDF)
    public String getTimestampFormatted() {
        if (timestamp == null) return "";
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ===== other fields =====

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
