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

public class AttendanceService {

    private final List<AttendanceObserver> observers = new ArrayList<>();
    private final List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private final Map<String, AttendanceRecord> records = new HashMap<>();
    private final double threshold = Double.parseDouble(Config.get("recognition.high.threshold"));
    private final AttendanceRecordRepository repo;

    public AttendanceService() {
        this.repo = new AttendanceRecordRepository();
    }

    public void addObserver(AttendanceObserver o) {
        observers.add(o);
    }

    // F_MA: modified by felicia handling marking attendance
    public synchronized void markAttendance(AttendanceRecord r) {
        try {
            // Already marked or high confidence → mark directly
            if (r.getConfidence() >= threshold) {
                saveAttendanceRecord(r);
                return;
            }

            // Low confidence → ask for user confirmation asynchronously
            AttendanceController.requestUserConfirmationAsync(r, confirmed -> {
                if (confirmed) {
                    saveAttendanceRecord(r);
                } else {
                    notifyAttendanceNotMarked(r);
                }
            });
        } catch (Exception e) {
            notifyAttendanceNotMarked(r);
        }
    }

    private void saveAttendanceRecord(AttendanceRecord r) {
        try {
            attendanceRecords.add(r);
            r.mark(observers);
        } catch (Exception e) {
            notifyAttendanceNotMarked(r);
        }
    }

    private void notifyAttendanceNotMarked(AttendanceRecord r) {
        for (AttendanceObserver o : observers) {
            if (o instanceof LiveRecognitionController) {
                ((LiveRecognitionController) o).onAttendanceNotMarked(r);
            }
        }
    }

    // public synchronized void markAttendance(AttendanceRecord r) {
    // boolean result = true;
    // try {
    // // if confidence < threshold, request user confirmation
    // if (r.getConfidence() < threshold) {
    // result = AttendanceController.requestUserConfirmation(r);
    // // return;
    // System.out.println("result " + result); // for testing
    // }
    // System.out.println("result " + result); // for testing
    // if (result) {
    // attendanceRecords.add(r);
    // System.out.println("run until here 1"); // for testing
    // r.mark(observers);
    // System.out.println("run until here 2"); // for testing
    // // for (AttendanceObserver o : observers) {
    // // // notify the recognitionService that the attendance of a particular
    // student is marked
    // // o.onAttendanceMarked(r);
    // // if (o instanceof AttendanceController) {
    // // // refresh the attendancceRecords page
    // // ((AttendanceController) o).loadAttendanceRecords();
    // // }
    // // }
    // } else {
    // for (AttendanceObserver o : observers) {
    // // notify the recognitionService that the attendance of a particular student
    // is NOT marked
    // if (o instanceof LiveRecognitionController) {
    // ((LiveRecognitionController) o).onAttendanceNotMarked(r);
    // }
    // }
    // }
    // } catch (Exception e) {
    // for (AttendanceObserver o : observers) {
    // // notify the recognitionService that the attendance of a particular student
    // is NOT marked
    // if (o instanceof LiveRecognitionController) {
    // ((LiveRecognitionController) o).onAttendanceNotMarked(r);
    // }
    // }
    // }
    // }
    // public synchronized void markAttendance(AttendanceRecord r) {
    // try {
    // attendanceRecords.add(r);
    // r.mark();
    // for (AttendanceObserver o : observers) {
    // // notify the recognitionService that the attendance of a particular student
    // is marked
    // o.onAttendanceMarked(r);
    // }
    // } catch (Exception e) {
    // for (AttendanceObserver o : observers) {
    // // notify the recognitionService that the attendance of a particular student
    // is NOT marked
    // if (o instanceof LiveRecognitionController) {
    // ((LiveRecognitionController) o).onAttendanceNotMarked(r);
    // }
    // }
    // }
    // }
    // public AttendanceRecord getOrCreateRecord(Student student, Session session) {
    // return records.computeIfAbsent(student.getStudentId(), id -> new
    // AttendanceRecord(student, session));
    // }
    // public AttendanceRecord getRecord(Student student, Session session) {
    // return records.computeIfAbsent(student.getStudentId(), id -> new
    // AttendanceRecord(student, session));
    // }
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

    // public void addObserver(AttendanceObserver o) {
    // observers.add(o);
    // }
    // public synchronized void markAttendance(AttendanceRecord r) {
    // records.add(r);
    // for (AttendanceObserver o : observers)
    // o.onAttendanceMarked(r);
    // }
    // public synchronized List<AttendanceRecord> getAll() {
    // return new ArrayList<>(records);
    // }
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

    public List<AttendanceRecord> findBySessionId(int session_id) {
        return repo.findBySessionId(session_id);
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

    public void saveRecord(AttendanceRecord record) {
        repo.save(record);
    }

    public String capitalize(String str) {
        return repo.capitalize(str);
    }

    public boolean isAlreadyMarked(int studentId, int sessionId) {
        AttendanceRecord existing = repo.findById(studentId, sessionId);
        return existing != null;
    }
}
