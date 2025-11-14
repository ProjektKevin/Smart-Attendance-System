/*
 # Modified by: Chue Wan Yan
 # Step: 3
 # Date: 13 Oct 2025
 */
package com.smartattendance.model.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;
import com.smartattendance.repository.AttendanceRecordRepository;
import com.smartattendance.service.AttendanceObserver;
import com.smartattendance.service.AutoAttendanceMarker;

public class AttendanceRecord {

    // these two make it easy to persist/auto-mark, but they also couple the entity
    // private AttendanceRecordRepository attendanceRecordRepo = new AttendanceRecordRepository();
    // private AutoAttendanceMarker autoAttendanceMarker = new AutoAttendanceMarker();

    private final Student student;
    private final Session session;

    private AttendanceStatus status;
    private LocalDateTime timestamp;
    private LocalDateTime lastSeen;
    private MarkMethod method;
    private double confidence;
    private String note;

    // for detecting changes
    private AttendanceStatus originalStatus;
    private String originalNote;

    // -------------------------------------------------------------------------
    // 1) constructor used when session is created (no last_seen yet)
    // -------------------------------------------------------------------------
    public AttendanceRecord(Student student,
                            Session session,
                            AttendanceStatus status,
                            double confidence,
                            MarkMethod method,
                            LocalDateTime timestamp) {
        this.student = student;
        this.session = session;
        this.status = status;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.lastSeen = timestamp;   // default: lastSeen = first timestamp
        this.method = method;
        this.note = "";
    }

    // -------------------------------------------------------------------------
    // 2) constructor used by repository (with BOTH marked_at and last_seen)
    //    THIS is the one your repository is calling.
    // -------------------------------------------------------------------------
    public AttendanceRecord(Student student,
                            Session session,
                            AttendanceStatus status,
                            double confidence,
                            MarkMethod method,
                            LocalDateTime timestamp,
                            LocalDateTime last_seen) {
        this.student = student;
        this.session = session;
        this.status = status;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.lastSeen = last_seen;
        this.method = method;
        this.note = "";
    }

    /**
     * Marks attendance via auto marker.
     */
    // public void mark(List<AttendanceObserver> observers) throws Exception {
    //     try {
    //         // System.out.println("run until here 3"); // for testing
    //         autoAttendanceMarker.markAttendance(observers, this);
    //     } catch (Exception e) {
    //         throw new Exception("Failed to save attendance record", e);
    //     }
    // }

    // -------------------------------------------------------------------------
    // getters / setters
    // -------------------------------------------------------------------------
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

    // public AttendanceRecordRepository getAttendanceRecordRepo() {
    //     return attendanceRecordRepo;
    // }

    public AttendanceStatus getOriginalStatus() {
        return originalStatus;
    }

    public void setOriginalStatus(AttendanceStatus originalStatus) {
        this.originalStatus = originalStatus;
    }

    public String getOriginalNote() {
        return originalNote;
    }

    public void setOriginalNote(String originalNote) {
        this.originalNote = originalNote;
    }

    public boolean isStatusChanged() {
        return !Objects.equals(originalStatus, status);
    }

    public boolean isNoteChanged() {
        return !Objects.equals(originalNote, note);
    }
}
