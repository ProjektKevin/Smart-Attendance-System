package com.smartattendance.service;

import java.util.ArrayList;
import java.util.List;

import com.smartattendance.config.Config;
import com.smartattendance.controller.AttendanceController;
// import com.smartattendance.controller.RecognitionController;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.repository.AttendanceRecordRepository;

/**
 * Service layer that coordinates attendance operations between repository,
 * recognition system, and observers.
 *
 * Follows the Observer and Strategy patterns. - AutoAttendanceMarker handles
 * automatic marking logic. - Observers (like UI controllers) respond to
 * attendance updates.
 *
 * @author Chue Wan Yan
 *
 * @version 13:23 15 Nov 2025
 */
public class AttendanceService {

    private final List<AttendanceObserver> observers = new ArrayList<>(); // Listof observers that listen for attendance related events
    // private final List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private final double threshold
            = Double.parseDouble(Config.get("recognition.high.threshold")); // Confidence threshold above which attendance is auto-confirmed
    private final AttendanceRecordRepository repo; // Repository for retrieve, update, save and delete AttendanceRecord
    private final AutoAttendanceMarker autoAttendanceMarker; // Responsible for auto attendance marking

    /**
     * Creates a new AttendanceService with AttendanceRepository and
     * AutoAttendanceMarker instance.
     */
    public AttendanceService() {
        this.repo = new AttendanceRecordRepository();
        this.autoAttendanceMarker = new AutoAttendanceMarker(this);
    }

    /**
     * Get AutoAttendanceMarker
     *
     * @return the AutoAttendanceMarker used by this service
     */
    public AutoAttendanceMarker getAutoAttendanceMarker() {
        return autoAttendanceMarker;
    }

    /**
     * Registers an observer to receive attendance events.
     *
     * @param observer the observer to add
     */
    public void addObserver(AttendanceObserver observer) {
        observers.add(observer);
    }

    /**
     * Notifies all observers that an attendance entry was successfully marked.
     *
     * @param message information describing the event
     */
    public void notifyMarked(String message) {
        for (AttendanceObserver observer : observers) {
            observer.onAttendanceMarked(message);
            
            // notify the recognitionService that the attendance of a particular student is marked
            // if (observer instanceof RecognitionObserver recognitionObserver) {
            //     recognitionObserver.onAttendanceMarked(message);
            // }

            // refresh the attendancceRecords page
            // if (observer instanceof AttendanceObserver attendanceObserver) {
            //     attendanceObserver.onAttendanceMarked(message);
            // }
        }
    }

    /**
     * Notifies only RecognitionObservers that an attendance mark was skipped
     * due to still in cooldown time.
     *
     * @param reason the reason for skipping
     */
    public void notifySkipped(String reason) {
        for (AttendanceObserver observer : observers) {
            // notify the recognitionService that the attendance of a particular student is skipping remark
            if (observer instanceof RecognitionObserver recognitionObserver) {
                recognitionObserver.onAttendanceSkipped(reason);
            }
        }
    }

    /**
     * Notifies RecognitionObservers that attendance was not marked,
     * due to a failure or rejected confirmation.
     *
     * @param record the record that was not marked
     */
    private void notifyAttendanceNotMarked(AttendanceRecord record) {
        for (AttendanceObserver observer : observers) {
            if (observer instanceof RecognitionObserver recognitionObserver) {
                recognitionObserver.onAttendanceNotMarked(record);
            }
        }
    }

    /**
     * Attempts to mark attendance. Delegates automatic marking to
     * AutoAttendanceMarker. (Mark via face detection)
     * 
     * 1. If confidence >= threshold -> mark attendancce
     * 2. If confidence < threshold -> request for user confirmation 
     *      a. If user confirmed -> mark attendance
     *      b. If user rejected or error occurs -> not mark attendance and notify user
     */
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

    /**
     * Saves the attendance record using the AutoAttendanceMarker.
     * If any error occurs, RecognitionObservers are notified.
     *
     * @param record the record to save
     */
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

    /**
     * Finds a single attendance record by student ID and session ID.
     *
     * @param studentId the ID of the student
     * @param sessionId the ID of the session
     * @return the matching AttendanceRecord, or {@code null} if none exists
     */
    public AttendanceRecord findById(int studentId, int sessionId) {
        return repo.findById(studentId, sessionId);
    }

     /**
     * Retrieves all attendance records associated with a given session.
     *
     * @param session_id the session ID to query
     * @return a list of AttendanceRecord objects for the session
     */
    public List<AttendanceRecord> findBySessionId(int session_id) {
        return repo.findBySessionId(session_id);
    }

    /**
     * Saves a new attendance record to the database.
     *
     * @param record the AttendanceRecord to save
     */
    public void saveRecord(AttendanceRecord record) {
        repo.save(record);
    }

    /**
     * Updates an existing attendance record in the database.
     *
     * @param record the AttendanceRecord containing updated values
     */
    public void updateRecord(AttendanceRecord record) {
        repo.update(record);
    }

    /**
     * Updates only the {@code lastSeen} timestamp of an attendance record.
     *
     * @param record the AttendanceRecord to update
     */
    public void updateLastSeen(AttendanceRecord record) {
        repo.updateLastSeen(record);
    }

    /**
     * Updates the attendance status (PRESENT, ABSENT, LATE) of a record.
     *
     * @param record the AttendanceRecord whose status should be updated
     */
    public void updateStatus(AttendanceRecord record) {
        repo.updateStatus(record);
    }

    /**
     * Updates the note associated with an attendance record.
     *
     * @param record the AttendanceRecord whose note should be updated
     */
    public void updateNote(AttendanceRecord record) {
        repo.updateNote(record);
    }

    /**
     * Deletes an attendance record from the database.
     *
     * @param record the AttendanceRecord to delete
     */
    public void deleteRecord(AttendanceRecord record) {
        repo.deleteRecord(record);
    }

    /**
     * Capitalizes the given string using repository helper logic.
     *
     * @param str the string to capitalize
     * @return the capitalized form of the string
     */
    public String capitalize(String str) {
        return repo.capitalize(str);
    }

    // public boolean isAlreadyMarked(int studentId, int sessionId) {
    //     AttendanceRecord existing = repo.findById(studentId, sessionId);
    //     return existing != null;
    // }
}
