/*
 # Modified by: Chue Wan Yan
 # Step: 9
 # Date: 18 Oct 2025
 */
package com.smartattendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartattendance.config.Config;
import com.smartattendance.controller.AttendanceController;
import com.smartattendance.controller.LiveRecognitionController;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.repository.AttendanceRecordRepository;
// import com.smartattendance.util.AttendanceObserver;

/**
 * Service layer that coordinates attendance operations between
 * repository, recognition system, and observers.
 *
 * Follows the Observer and Strategy patterns.
 * - AutoAttendanceMarker handles automatic marking logic.
 * - Observers (like UI controllers) respond to attendance updates.
 */

public class AttendanceService {

    private final List<AttendanceObserver> observers = new ArrayList<>();
    private final List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private final Map<String, AttendanceRecord> records = new HashMap<>();
    private final double threshold = Double.parseDouble(Config.get("recognition.threshold"));
    private final AttendanceRecordRepository repo;
    private final AutoAttendanceMarker autoAttendanceMarker;

    public AttendanceService(){
        this.repo = new AttendanceRecordRepository();
        this.autoAttendanceMarker = new AutoAttendanceMarker();
    }

    public void addObserver(AttendanceObserver o) {
        observers.add(o);
    }

    /**
     * Attempts to mark attendance.
     * Delegates automatic marking to AutoAttendanceMarker.
     */
    // F_MA: modified by felicia handling marking attendance
    public synchronized void markAttendance(AttendanceRecord record) {
        try {
            // Already marked or high confidence → mark directly
            if (record.getConfidence() >= threshold) {
                saveAttendanceRecord(record);
                return;
            }

            // Low confidence → ask for user confirmation asynchronously
            AttendanceController.requestUserConfirmationAsync(record, confirmed -> {
                if (confirmed) {
                    saveAttendanceRecord(record);
                } else {
                    notifyAttendanceNotMarked(record);
                }
            });
        } catch (Exception e) {
            notifyAttendanceNotMarked(record);
        }
    }

    private void saveAttendanceRecord(AttendanceRecord record) {
        try {
            attendanceRecords.add(record);
            // r.mark(observers);
            autoAttendanceMarker.markAttendance(observers, record);
        } catch (Exception e) {
            notifyAttendanceNotMarked(record);
        }
    }

    private void notifyAttendanceNotMarked(AttendanceRecord record) {
        for (AttendanceObserver o : observers) {
            if (o instanceof LiveRecognitionController) {
                ((LiveRecognitionController) o).onAttendanceNotMarked(record);
            }
        }
    }

    public void updateLastSeen(String studentId, LocalDateTime time) {
        if (records.containsKey(studentId)) {
            records.get(studentId).setLastSeen(time);
        }
    }

    public synchronized Map<String, AttendanceRecord> getAll() {
        return Collections.unmodifiableMap(new HashMap<>(records));
    }

    public synchronized void printAllRecords() {
        for (Map.Entry<String, AttendanceRecord> entry : records.entrySet()) {
            String studentId = entry.getKey();
            AttendanceRecord record = entry.getValue();

            System.out.printf("Student ID: %s | Name: %s | Status: %s | Method: %s | Last Seen: %s%n",
                    studentId,
                    record.getStudent().getName(),
                    record.getStatus(),
                    record.getMethod(),
                    record.getLastSeen());
        }
    }

    public synchronized List<AttendanceRecord> getBetween(LocalDate from, LocalDate to) {
        List<AttendanceRecord> out = new ArrayList<>();
        for (AttendanceRecord r : attendanceRecords) {
            LocalDate d = r.getTimestamp().toLocalDate();
            boolean ok = (from == null || !d.isBefore(from)) && (to == null || !d.isAfter(to));
            if (ok) {
                out.add(r);
            }
        }
        return out;
    }

    public List<AttendanceRecord> findBySessionId(int session_id){
        return repo.findBySessionId(session_id);
    }

    public void updateStatus(AttendanceRecord record){
        repo.updateStatus(record);
    }

    public void updateNote(AttendanceRecord record){
        repo.updateNote(record);
    }

    public void deleteRecord(AttendanceRecord record){
        repo.deleteRecord(record);
    }

    public void saveRecord(AttendanceRecord record){
        repo.save(record);
    }

    public String capitalize(String str) {
        return repo.capitalize(str);
    }
}
