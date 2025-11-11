package com.smartattendance.model.dto.report;

import java.time.LocalDate;

public class AttendanceReportFilter {

    private LocalDate fromDate;
    private LocalDate toDate;
    private String courseCode;
    private String sessionId;
    private String status;
    private String method;
    /**
     * We will store things like:
     *  - "All"
     *  - ">= 0.90"
     *  - ">= 0.80"
     *  - "< 0.80"
     *  - "0.95"
     */
    private String confidence;

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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
}
