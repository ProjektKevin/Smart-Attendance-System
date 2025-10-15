package com.smartattendance.model;

import java.time.LocalDateTime;

public class AttendanceTracker {
    public enum Status {
        PENDING, PRESENT, ABSENT, LATE
    }

    private final Student student;
    private Status status = Status.PENDING;
    private LocalDateTime timestamp;
    private String method; // e.g., "manual", "QR scan", "RFID"

    public AttendanceTracker(Student student) {
        this.student = student;
    }

    public void mark(Status status, String method) {
        this.status = status;
        this.method = method;
        this.timestamp = LocalDateTime.now();
    }

    public Student getStudent() {
        return student;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMethod() {
        return method;
    }
}
