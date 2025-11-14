/*
 # Modified by: Chue Wan Yan
 # Step: 9
 # Date: 18 Oct 2025
 */
package com.smartattendance.service;

import java.util.ArrayList;
import java.util.List;

import com.smartattendance.config.Config;
import com.smartattendance.controller.AttendanceController;
import com.smartattendance.controller.RecognitionController;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.repository.AttendanceRecordRepository;

/**
 * Service layer that coordinates attendance operations between
 * repository, recognition system, and observers.
 * @author Chue Wan Yan
 *
 * Follows the Observer and Strategy patterns.
 * - AutoAttendanceMarker handles automatic marking logic.
 * - Observers (like UI controllers) respond to attendance updates.
 */
public class AttendanceService {

    private final List<AttendanceObserver> observers = new ArrayList<>();
    // private final List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private final double threshold = Double.parseDouble(Config.get("recognition.high.threshold"));
    private final AttendanceRecordRepository repo;
    private final AutoAttendanceMarker autoAttendanceMarker;

    public AttendanceService() {
        this.repo = new AttendanceRecordRepository();
        this.autoAttendanceMarker = new AutoAttendanceMarker(this);
    }

    public AutoAttendanceMarker getAutoAttendanceMarker() {
        return autoAttendanceMarker;
    }

    public void addObserver(AttendanceObserver o) {
        observers.add(o);
    }

    public void notifyMarked(List<AttendanceObserver> observers, AttendanceRecord record, String message) {
        for (AttendanceObserver o : observers) {
            // notify the recognitionService that the attendance of a particular student is marked
            if (o instanceof RecognitionController rc) {
                rc.onAttendanceMarked(record, message);
            }

            // refresh the attendancceRecords page
            if (o instanceof AttendanceController ac) {
                ac.loadAttendanceRecords();
            }
        }
    }

    public void notifySkipped(List<AttendanceObserver> observers, AttendanceRecord record, String reason) {
        for (AttendanceObserver o : observers) {
            // notify the recognitionService that the attendance of a particular student is skipping remark
            if (o instanceof RecognitionController rc) {
                rc.onAttendanceSkipped(record, reason);
            }
        }
    }

    private void notifyAttendanceNotMarked(AttendanceRecord r) {
        for (AttendanceObserver o : observers) {
            if (o instanceof RecognitionController) {
                ((RecognitionController) o).onAttendanceNotMarked(r);
            }
        }
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
            // attendanceRecords.add(r);
            // record.mark(observers);
            autoAttendanceMarker.markAttendance(observers, record);
            // System.out.println("Tehse are the observers:" + observers);// F_MA: for testing
        } catch (Exception e) {
            notifyAttendanceNotMarked(record);
        }
    }

    public AttendanceRecord findById(int studentId, int sessionId) {
        return repo.findById(studentId, sessionId);
    }

    public List<AttendanceRecord> findBySessionId(int session_id) {
        return repo.findBySessionId(session_id);
    }

    public void saveRecord(AttendanceRecord record) {
        repo.save(record);
    }

    public void updateRecord(AttendanceRecord record) {
        repo.update(record);
    }

    public void updateLastSeen(AttendanceRecord record) {
        repo.updateLastSeen(record);
    }

    public void updateStatus(AttendanceRecord record) {
        repo.updateStatus(record);
    }

    public void updateNote(AttendanceRecord record) {
        repo.updateNote(record);
    }

    public void deleteRecord(AttendanceRecord record) {
        repo.deleteRecord(record);
    }

    public String capitalize(String str) {
        return repo.capitalize(str);
    }

    // public boolean isAlreadyMarked(int studentId, int sessionId) {
    //     AttendanceRecord existing = repo.findById(studentId, sessionId);
    //     return existing != null;
    // }
}
