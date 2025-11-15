package com.smartattendance.model.dto.report;

import java.time.LocalDate;

/**
 * Filter object used by the reporting module to narrow down which
 * attendance records should be included in a generated report.
 *
 * <p>Typical usage:
 * <ul>
 *     <li>Report controller/popups collect user-selected filter values</li>
 *     <li>Those values are stored in an {@code AttendanceReportFilter}</li>
 *     <li>The repository/service layer reads this object and applies the filters
 *         in SQL or in-memory filtering before building {@link com.smartattendance.util.report.AttendanceReportRow} instances</li>
 * </ul>
 */
public class AttendanceReportFilter {

    /**
     * Start of the report date range (inclusive).
     * May be {@code null} to indicate "no lower bound".
     */
    private LocalDate fromDate;

    /**
     * End of the report date range (inclusive).
     * May be {@code null} to indicate "no upper bound".
     */
    private LocalDate toDate;

    /**
     * Optional course code filter. If {@code null} or blank,
     * records from all courses are included.
     */
    private String courseCode;

    /**
     * Optional session identifier. If set, only records for this session
     * are included; if {@code null} or blank, all sessions are considered.
     */
    private String sessionId;

    /**
     * Optional attendance status filter (e.g. "Present", "Late", "Absent", "Pending").
     * If {@code null} or blank, all statuses are included.
     */
    private String status;

    /**
     * Optional method filter (e.g. "Manual", "Face Recognition").
     * If {@code null} or blank, records with any method are included.
     */
    private String method;

    /**
     * Optional confidence filter expressed as a string.
     *
     * <p>Intended values (as used by the UI) include:
     * <ul>
     *     <li>{@code "All"}</li>
     *     <li>{@code ">= 0.90"}</li>
     *     <li>{@code ">= 0.80"}</li>
     *     <li>{@code "< 0.80"}</li>
     *     <li>{@code "0.95"} (exact value)</li>
     * </ul>
     *
     * <p>The service/repository layer is responsible for interpreting this string
     * and applying the corresponding numeric filter to confidence values.
     */
    private String confidence;

    /**
     * @return the start date (inclusive) of the report range, or {@code null} if none
     */
    public LocalDate getFromDate() {
        return fromDate;
    }

    /**
     * @param fromDate start date (inclusive) of the report range; {@code null} for no lower bound
     */
    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * @return the end date (inclusive) of the report range, or {@code null} if none
     */
    public LocalDate getToDate() {
        return toDate;
    }

    /**
     * @param toDate end date (inclusive) of the report range; {@code null} for no upper bound
     */
    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    /**
     * @return the course code filter, or {@code null}/blank if all courses are allowed
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * @param courseCode course code to filter by; {@code null}/blank to include all courses
     */
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    /**
     * @return the session ID filter, or {@code null}/blank if all sessions are allowed
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId session ID to filter by; {@code null}/blank to include all sessions
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the status filter (e.g. "Present"), or {@code null}/blank for all statuses
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status attendance status to filter by; {@code null}/blank to include all statuses
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the method filter (e.g. "Manual"), or {@code null}/blank for all methods
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method method to filter by (e.g. "Manual", "Face Recognition");
     *               {@code null}/blank to include all methods
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return the confidence filter string, or {@code null}/blank meaning "no filter"
     */
    public String getConfidence() {
        return confidence;
    }

    /**
     * @param confidence confidence filter string (e.g. "All", ">= 0.90", "< 0.80", "0.95")
     */
    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}
