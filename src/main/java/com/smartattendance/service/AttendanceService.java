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

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;
import com.smartattendance.controller.AttendanceController;
import com.smartattendance.controller.LiveRecognitionController;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.util.AttendanceObserver;

public class AttendanceService {
    private final List<AttendanceObserver> observers = new ArrayList<>();
    private final List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private final Map<String, AttendanceRecord> records = new HashMap<>();
    private final double threshold = Double.parseDouble(Config.get("recognition.threshold"));

    public void addObserver(AttendanceObserver o) {
        observers.add(o);
    }

    // F_MA: modified by felicia handling marking attendance
    public synchronized void markAttendance(AttendanceRecord r) {
        try {
            // if confidence < threshold, request user confirmation
            if (r.getConfidence() < threshold) {
                AttendanceController.requestUserConfirmation(r);
                return;
            }
            attendanceRecords.add(r);
            r.mark();
            for (AttendanceObserver o : observers) {
                // notify the recognitionService that the attendance of a particular student is marked
                o.onAttendanceMarked(r);
                if (o instanceof AttendanceController) {
                    // refresh the attendancceRecords page
                    ((AttendanceController) o).loadAttendanceRecords();
                }
            }
        } catch (Exception e) {
            for (AttendanceObserver o : observers) {
                // notify the recognitionService that the attendance of a particular student is NOT marked
                if (o instanceof LiveRecognitionController) {
                    ((LiveRecognitionController) o).onAttendanceNotMarked(r);
                }
            }
        }
    }

    // public synchronized void markAttendance(AttendanceRecord r) {
    //     try {
    //         attendanceRecords.add(r);
    //         r.mark();
    //         for (AttendanceObserver o : observers) {
    //             // notify the recognitionService that the attendance of a particular student is marked
    //             o.onAttendanceMarked(r);
    //         }
    //     } catch (Exception e) {
    //         for (AttendanceObserver o : observers) {
    //             // notify the recognitionService that the attendance of a particular student is NOT marked
    //             if (o instanceof LiveRecognitionController) {
    //                 ((LiveRecognitionController) o).onAttendanceNotMarked(r);
    //             }
    //         }
    //     }
    // }

    // public AttendanceRecord getOrCreateRecord(Student student, Session session) {
    //     return records.computeIfAbsent(student.getStudentId(), id -> new AttendanceRecord(student, session));
    // }

    // public AttendanceRecord getRecord(Student student, Session session) {
    //     return records.computeIfAbsent(student.getStudentId(), id -> new AttendanceRecord(student, session));
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
    //   observers.add(o);
    // }
    // public synchronized void markAttendance(AttendanceRecord r) {
    //   records.add(r);
    //   for (AttendanceObserver o : observers)
    //     o.onAttendanceMarked(r);
    // }
    // public synchronized List<AttendanceRecord> getAll() {
    //   return new ArrayList<>(records);
    // }
    public synchronized List<AttendanceRecord> getBetween(LocalDate from, LocalDate to) {
      List<AttendanceRecord> out = new ArrayList<>();
      for (AttendanceRecord r : attendanceRecords) {
        LocalDate d = r.getTimestamp().toLocalDate();
        boolean ok = (from == null || !d.isBefore(from)) && (to == null || !d.isAfter(to));
        if (ok)
          out.add(r);
      }
      return out;
    }
}
