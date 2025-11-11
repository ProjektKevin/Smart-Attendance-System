package com.smartattendance.model.dto.dashboard;

import java.time.LocalDate;

/**
 * DTO version of AttendanceRow, moved out of the controller layer.
 * This matches what AttendanceRecordRepository.constructs in findByStudentId(...):
 * new AttendanceRow(sessionDate, startTime, endTime, courseCode, courseName, status)
 */
public class AttendanceRow {

    private LocalDate sessionDate;
    private String startTime;
    private String endTime;
    private String courseCode;
    private String courseName;
    private String status;

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

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
