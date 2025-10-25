/*
 # Modified by: Chue Wan Yan
 # Step: 3
 # Date: 13 Oct 2025
 */
package com.smartattendance.model;

import java.time.LocalDateTime;

import com.smartattendance.repository.AttendanceRecordRepository;

public class AttendanceRecord {
    private AttendanceRecordRepository attendanceRecordRepo = new AttendanceRecordRepository();
    private final Student student;
    private final Session session;
    // private final String status;
    private AttendanceStatus status;
    private LocalDateTime timestamp;
    private LocalDateTime lastSeen;
    private MarkMethod method;
    // private final String method; 
    private double confidence; 
    // private final LocalDateTime timestamp; 
    private String note;

    // public AttendanceRecord(Student s, Session sess, AttendanceStatus st, MarkMethod m, double c, LocalDateTime ts){ 
    //   this.student=s; 
    //   this.session=sess; 
    //   this.status=st; 
    //   this.method=m; 
    //   this.confidence=c; 
    //   this.timestamp=ts; 
    // }
    public AttendanceRecord(Student student, Session session, AttendanceStatus status, double confidence, LocalDateTime timestamp, MarkMethod method) {
        this.student = student;
        this.session = session;
        // this.status = AttendanceStatus.ABSENT;
        this.status = status;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.lastSeen = timestamp;
        this.method = method;
        this.note = "";
    }

    /**
     * Marks attendance with all relevant info.
     */
    public void mark() {
        this.status = status;
        this.timestamp = timestamp;
        this.lastSeen = timestamp;
        this.method = method;
        if (note != null) {
            this.note = note;
        }

        attendanceRecordRepo.save(this); // modify this

    }
    // public void mark(AttendanceStatus status, LocalDateTime timestamp, MarkMethod method, String notes) {
    //     this.status = status;
    //     this.timestamp = timestamp;
    //     this.lastSeen = timestamp;
    //     this.method = method;
    //     if (note != null) {
    //         this.note = note;
    //     }
    // }

    public Student getStudent() {
        return student;
    }

    public Session getSession() {
        return session;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public MarkMethod getMethod() {
        return method;
    }

    public void setMethod(MarkMethod method) {
        this.method = method;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

}
