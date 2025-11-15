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
// import com.smartattendance.controller.RecognitionController;
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

    public void addObserver(AttendanceObserver observer) {
        observers.add(observer);
    }

    public void notifyMarked(String message) {
        for (AttendanceObserver observer : observers) {
            // notify the recognitionService that the attendance of a particular student is marked
            // if (observer instanceof RecognitionObserver recognitionObserver) {
            //     recognitionObserver.onAttendanceMarked(message);
            // }

            // refresh the attendancceRecords page
            // if (observer instanceof AttendanceObserver attendanceObserver) {
            //     attendanceObserver.onAttendanceMarked(message);
            // }

            observer.onAttendanceMarked(message);
        }
    }

    public void notifySkipped(String reason) {
        for (AttendanceObserver observer : observers) {
            // notify the recognitionService that the attendance of a particular student is skipping remark
            if (observer instanceof RecognitionObserver recognitionObserver) {
                recognitionObserver.onAttendanceSkipped(reason);
            }
        }
    }

    private void notifyAttendanceNotMarked(AttendanceRecord record) {
        for (AttendanceObserver observer : observers) {
            if (observer instanceof RecognitionObserver recognitionObserver) {
                recognitionObserver.onAttendanceNotMarked(record);
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
            for (AttendanceObserver observer : observers) {
                if (observer instanceof RecognitionObserver recognitionObserver) {
                    recognitionObserver.requestUserConfirmationAsync(record, confirmed -> {
                        if (confirmed) {
                            saveAttendanceRecord(record);
                        } else {
                            notifyAttendanceNotMarked(record);
                        }
                    });
                    break;
                }
            }
        } catch (Exception e) {
            notifyAttendanceNotMarked(record);
        }
    }

    private void saveAttendanceRecord(AttendanceRecord record) {
        try {
            // attendanceRecords.add(r);
            // record.mark(observers);
            autoAttendanceMarker.markAttendance(record);
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
