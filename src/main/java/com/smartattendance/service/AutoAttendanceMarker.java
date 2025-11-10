/*
 # Done by: Chue Wan Yan
 # Step: 2
 # Date: 13 Oct 2025
 */
package com.smartattendance.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.smartattendance.config.Config;
import com.smartattendance.controller.AttendanceController;
import com.smartattendance.controller.LiveRecognitionController;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;
import com.smartattendance.repository.AttendanceRecordRepository;
import com.smartattendance.repository.SessionRepository;
import com.smartattendance.util.ControllerRegistry;

import javafx.application.Platform;

/**
 * Automatically marks attendance using face recognition confidence values.
 */
public class AutoAttendanceMarker implements AttendanceMarker {

    private int cooldownSeconds = Integer.parseInt(Config.get("cooldown.seconds"));
    private final AttendanceRecordRepository attendanceRecordRepo = new AttendanceRecordRepository();
    
    @Override
    public void markAttendance(List<AttendanceObserver> observers, AttendanceRecord record) throws Exception {


        try {
            Student student = record.getStudent();
            Session session = record.getSession();

            AttendanceRecord existingRecord = attendanceRecordRepo.findById(student.getStudentId(), session.getSessionId());

            LocalDateTime now = record.getTimestamp();

            // First time marking
            // if the student is not marked as present before (still in default PENDING status), update the student attendance
            if (existingRecord.getStatus() == AttendanceStatus.PENDING) {

                // if the student is late, set attendance status to LATE before update
                long minutesLate = Duration.between(session.getStartTime(), now).toMinutes();
                AttendanceStatus status = (minutesLate > session.getLateThresholdMinutes())
                        ? AttendanceStatus.LATE
                        : AttendanceStatus.PRESENT;
                record.setStatus(status);
                record.setTimestamp(now);
                record.setMethod(MarkMethod.AUTO);
                record.setNote("Auto-marked via face recognition");

                attendanceRecordRepo.update(record);
                String message = "Marked " + student.getName() + " (Student Id: " + student.getStudentId() + ")" + " as " + status;
                for (AttendanceObserver o : observers) {
                    if (o instanceof LiveRecognitionController) {
                        ((LiveRecognitionController) o).onAttendanceMarked(record, message);
                    }
                    if (o instanceof AttendanceController) {
                        // refresh the attendancceRecords page
                        ((AttendanceController) o).loadAttendanceRecords();
                    }
                }

            // student already marked then update last seen
            } else if (existingRecord.getStatus() == AttendanceStatus.PRESENT || existingRecord.getStatus() == AttendanceStatus.LATE) {
                long secondsSinceLastSeen = Duration.between(existingRecord.getLastSeen(), now).getSeconds();
                String message = "Updated last seen for " + student.getName() + " (StudentId: " + student.getStudentId() + ")";
                if (secondsSinceLastSeen >= cooldownSeconds) {
                    existingRecord.setLastSeen(now);
                    attendanceRecordRepo.updateLastSeen(record);
                    for (AttendanceObserver o : observers) {
                        // notify the recognitionService that the attendance of a particular student is marked
                        if (o instanceof LiveRecognitionController) {
                            ((LiveRecognitionController) o).onAttendanceMarked(record, message);
                        }
                        if (o instanceof AttendanceController) {
                            // refresh the attendancceRecords page
                            ((AttendanceController) o).loadAttendanceRecords();
                        }
                    }
                } else {
                    for (AttendanceObserver o : observers) {
                        // notify the recognitionService that the attendance of a particular student is not remarked
                        if (o instanceof LiveRecognitionController) {
                            ((LiveRecognitionController) o).onAttendanceSkipped(record, "Cooldown active for " + student.getName() + " (Student Id: " + student.getStudentId() + ")" + ", skipping re-mark.");
                        }
                    }
                }
            } else {
                for (AttendanceObserver o : observers) {
                    // notify the recognitionService that the attendance of a particular student is not remarked
                    if (o instanceof LiveRecognitionController) {
                        ((LiveRecognitionController) o).onAttendanceSkipped(record, "Skipping " + student.getName() + " (Student Id: " + student.getStudentId() + ")" + ": already marked as " + existingRecord.getStatus());
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to mark attendance: ", e);
        }

    }

    /**
     * Core logic to mark pending attendance as ABSENT
     */
    public static void markPendingAttendanceAsAbsent(SessionRepository sessionRepository, AttendanceService attendanceService) {
        AttendanceRecordRepository attendanceRecordRepo = new AttendanceRecordRepository();
        List<Session> allSessions = sessionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Session sess : allSessions) {
            boolean isClosed = "Closed".equalsIgnoreCase(sess.getStatus());

            if (isClosed) {
                List<AttendanceRecord> pendingRecords = attendanceRecordRepo.findPendingAttendanceBySessionId(sess.getSessionId(), AttendanceStatus.PENDING);

                if (pendingRecords != null) {
                    for (AttendanceRecord rec : pendingRecords) {
                        rec.setStatus(AttendanceStatus.ABSENT);
                        rec.setMethod(MarkMethod.AUTO);
                        rec.setTimestamp(now);
                        rec.setNote("Auto-marked as Absent after session closed");
                        attendanceRecordRepo.update(rec);

                        System.out.println("Updated attendance to ABSENT for student: " + rec.getStudent().getName());
                    }
                }
            }
        }

        // Refresh the UI after updates
        AttendanceController attendanceController = ControllerRegistry.getAttendanceController();
        if (attendanceController != null) {
            Platform.runLater(attendanceController::loadAttendanceRecords);
        }
    }

}
