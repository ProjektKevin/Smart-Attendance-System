package com.smartattendance.controller.student;

import java.time.LocalDate;

public class StudentAttendanceRow {

    private final LocalDate date;
    private final String course;
    private final String status;
    private final String method;
    private final String markedAt;

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

    public LocalDate getDate() {
        return date;
    }

    public String getCourse() {
        return course;
    }

    public String getStatus() {
        return status;
    }

    public String getMethod() {
        return method;
    }

    public String getMarkedAt() {
        return markedAt;
    }
}
