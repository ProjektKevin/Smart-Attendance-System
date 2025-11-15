package com.smartattendance.model.entity;

import java.time.LocalDateTime;
// import java.util.List;
import java.util.Objects;

import com.smartattendance.controller.TabRefreshable;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;
// import com.smartattendance.repository.AttendanceRecordRepository;
// import com.smartattendance.service.AttendanceObserver;
// import com.smartattendance.service.AutoAttendanceMarker;
import com.smartattendance.service.AttendanceObserver;

/**
 * Represents a particular attendance record for a student in a session.
 *
 * Supports tracking changes for UI purposes via {@link #originalStatus} and
 * {@link #originalNote}.
 *
 * @author Chue Wan Yan
 *
 * @version 22:11 14 Nov 2025
 *
 */
public class AttendanceRecord {

    // private AttendanceRecordRepository attendanceRecordRepo = new AttendanceRecordRepository();
    // private AutoAttendanceMarker autoAttendanceMarker = new AutoAttendanceMarker();
    // ======= Data =======
    private final Student student; // The student of the attendance record
    private final Session session; // The session of the attendance record

    private AttendanceStatus status; // Student's attendance status
    private LocalDateTime timestamp; // Student's attendance marking time
    private LocalDateTime lastSeen; // Student's last seen time
    private MarkMethod method; // Method used to mark attendance
    private double confidence; // Confidence level of recognition for auto marking
    private String note; // Notes for attendance marking

    // ======= For detecting changes =======
    private AttendanceStatus originalStatus;
    private String originalNote;

    // ======= Constructors =======
    /**
     * Constructs an attendance record for a session when first created or
     * manual created by user.
     *
     * Last seen timestamp defaults to the provided timestamp.
     *
     * @param student The student associated with this record
     * @param session The session in which attendance is recorded
     * @param status Initial attendance status
     * @param confidence Confidence score of recognition
     * @param method Method of marking attendance
     * @param timestamp Marking time of attendance
     */
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
        this.lastSeen = timestamp; // default: lastSeen = timestamp when session auto create attendance record
        this.method = method;
        this.note = "";
    }

    /**
     * Constructs an attendance record loaded from a repository, including both
     * timestamp and last seen time.
     *
     * @param student The student associated with this record
     * @param session The session in which attendance is recorded
     * @param status Attendance status
     * @param confidence Confidence score of recognition
     * @param method Method of marking attendance
     * @param timestamp Timestamp of attendance
     * @param last_seen Last time the student was seen
     */
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
    // ======= Getters and Setters =======
    /**
     * Returns the student associated with this attendance record.
     *
     * @return The {@link Student} object
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Returns the session associated with this attendance record.
     *
     * @return The {@link Session} object
     */
    public Session getSession() {
        return session;
    }

    /**
     * Returns the attendance status.
     *
     * @return The {@link AttendanceStatus}
     */
    public AttendanceStatus getStatus() {
        return status;
    }

    /**
     * Updates the attendance status.
     *
     * @param status The new {@link AttendanceStatus}
     */
    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    /**
     * Returns the timestamp when attendance was first marked.
     *
     * @return The {@link LocalDateTime} timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the attendance record.
     *
     * @param timestamp The new timestamp
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the last time the student was seen.
     *
     * @return {@link LocalDateTime} of last seen
     */
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    /**
     * Updates the last seen timestamp.
     *
     * @param lastSeen The new last seen time
     */
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * Returns the method used to mark attendance.
     *
     * @return {@link MarkMethod} of attendance
     */
    public MarkMethod getMethod() {
        return method;
    }

    /**
     * Updates the attendance marking method.
     *
     * @param method The new {@link MarkMethod}
     */
    public void setMethod(MarkMethod method) {
        this.method = method;
    }

    /**
     * Returns the note associated with this attendance record.
     *
     * @return Note text
     */
    public String getNote() {
        return note;
    }

    /**
     * Updates the note for this attendance record.
     *
     * @param note The new note
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Returns the recognition confidence score.
     *
     * @return Confidence value
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * Updates the recognition confidence score.
     *
     * @param confidence New confidence value
     */
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    // public AttendanceRecordRepository getAttendanceRecordRepo() {
    //     return attendanceRecordRepo;
    // }
    /**
     * Returns the original attendance status for detecting changes.
     *
     * @return Original {@link AttendanceStatus}
     */
    public AttendanceStatus getOriginalStatus() {
        return originalStatus;
    }

    /**
     * Sets the original attendance status for change tracking.
     *
     * @param originalStatus Original {@link AttendanceStatus}
     */
    public void setOriginalStatus(AttendanceStatus originalStatus) {
        this.originalStatus = originalStatus;
    }

    /**
     * Returns the original note for detecting changes.
     *
     * @return Original note text
     */
    public String getOriginalNote() {
        return originalNote;
    }

    /**
     * Updates the original note for change tracking.
     *
     * @param originalNote Original note text
     */
    public void setOriginalNote(String originalNote) {
        this.originalNote = originalNote;
    }

    /**
     * Returns whether the attendance status has changed from its original.
     *
     * @return {@code true} if status differs from original, {@code false}
     * otherwise
     */
    public boolean isStatusChanged() {
        return !Objects.equals(originalStatus, status);
    }

    /**
     * Returns whether the note has changed from its original value.
     *
     * @return {@code true} if note differs from original, {@code false}
     * otherwise
     */
    public boolean isNoteChanged() {
        return !Objects.equals(originalNote, note);
    }
}
