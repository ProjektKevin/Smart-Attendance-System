package com.smartattendance.service;

import java.time.LocalDateTime;
import java.util.List;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;
import com.smartattendance.controller.AttendanceController;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;
import com.smartattendance.repository.AttendanceRecordRepository;
import com.smartattendance.repository.SessionRepository;
import com.smartattendance.util.AttendanceTimeUtils;
import com.smartattendance.util.ControllerRegistry;

import javafx.application.Platform;

/**
 * Automatically marks attendance using face recognition confidence values.
 * @version 18:18 14 Nov 2025
 * @author Chue Wan Yan
 */
public class AutoAttendanceMarker implements AttendanceMarker {

    // private final int lateThresholdMinutes = 15;
    // private final int cooldownSeconds = 30;
    private final int cooldownSeconds = Integer.parseInt(Config.get("cooldown.seconds")); // Extract cooldown time from config file
    private final AttendanceRecordRepository attendanceRecordRepo = new AttendanceRecordRepository(); // Repository to query database for attendance related operations
    private final AttendanceService attendanceService = ApplicationContext.getAttendanceService(); // Shared attendance service

    @Override
    public void markAttendance(List<AttendanceObserver> observers, AttendanceRecord record) throws Exception {
        try {
            // face recognition will only predict faces based on the roster, so no need to check again
            // if (!isInRoster(record.getSession(), record.getStudent().getStudentId())) {
            //     return;
            // }

            Student student = record.getStudent();
            Session session = record.getSession();
            LocalDateTime now = record.getTimestamp();

            AttendanceRecord existingRecord = attendanceService.findById(student.getStudentId(), session.getSessionId());
            if (existingRecord == null) {
                existingRecord = record;
            }

            String lastSeenMessage = "Updated last seen for " + student.getName() + " (StudentId: " + student.getStudentId() + ")";
            String coolDownMessage = "Cooldown active for " + student.getName() + " (Student Id: " + student.getStudentId() + ")" + ", skipping re-mark.";
            String skippingMessage = "Skipping " + student.getName() + " (Student Id: " + student.getStudentId() + ")" + ": already marked as " + existingRecord.getStatus();

            // if the student is not marked as present before (still in default PENDING status), update the student attendance
            if (existingRecord.getStatus() == AttendanceStatus.PENDING) {
                // if the student is late, set attendance status to LATE before update
                long minutesLate = AttendanceTimeUtils.minutesBetween(session.getStartTime(), now);
                AttendanceStatus status = (minutesLate > session.getLateThresholdMinutes())
                        ? AttendanceStatus.LATE 
                        : AttendanceStatus.PRESENT;
                String message = "Marked " + student.getName() + " (Student Id: " + student.getStudentId() + ")" + " as " + status;

                record.setStatus(status);
                record.setTimestamp(now);
                record.setMethod(MarkMethod.AUTO);
                record.setNote("Auto-marked via face recognition");

                attendanceService.updateRecord(record);
                attendanceService.notifyMarked(observers, record, message);

                // student already marked then update last seen
            } else if (existingRecord.getStatus() == AttendanceStatus.PRESENT || existingRecord.getStatus() == AttendanceStatus.LATE) {
                long secondsSinceLastSeen = AttendanceTimeUtils.secondsBetween(existingRecord.getLastSeen(), now);
                
                if (secondsSinceLastSeen >= cooldownSeconds) {
                    existingRecord.setLastSeen(now);
                    attendanceService.updateLastSeen(record);
                    attendanceService.notifyMarked(observers, record, lastSeenMessage);
                } else {
                    attendanceService.notifyMarked(observers, record, coolDownMessage);
                }
            } else {
                attendanceService.notifySkipped(observers, record, skippingMessage);
            }
        } catch (Exception e) {
            throw new Exception("Failed to mark attendance: ", e);
        }

    }

    /**
     * Marks all pending attendance records as ABSENT for sessions that are closed.
     *
     * @param sessionRepository repository to fetch all sessions
     */
    public void markPendingAttendanceAsAbsent(SessionRepository sessionRepository) {
        // AttendanceRecordRepository attendanceRecordRepo = new AttendanceRecordRepository();
        List<Session> allSessions = sessionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Session session : allSessions) {
            if (isSessionClosed(session)) {
                updatePendingRecords(session, now);
            }
        }

        refreshAttendanceUI();
    }

    /**
     * Checks if a session is closed.
     *
     * @param session the session to check
     * @return true if session is closed, false otherwise
     */
    private static boolean isSessionClosed(Session session) {
        return "Closed".equalsIgnoreCase(session.getStatus());
    }

    /**
     * Updates all pending attendance records for a given session to ABSENT.
     *
     * @param session the session whose pending records will be updated
     * @param timestamp the timestamp to set for marking
     */
    private void updatePendingRecords(Session session, LocalDateTime timestamp) {
        List<AttendanceRecord> pendingRecords = attendanceRecordRepo
                .findPendingAttendanceBySessionId(session.getSessionId(), AttendanceStatus.PENDING);

        if (pendingRecords != null) {
            for (AttendanceRecord record : pendingRecords) {
                record.setStatus(AttendanceStatus.ABSENT);
                record.setMethod(MarkMethod.AUTO);
                record.setTimestamp(timestamp);
                record.setNote("Auto-marked as Absent after session closed");

                attendanceRecordRepo.update(record);
                System.out.println("Updated attendance to ABSENT for student: " + record.getStudent().getName());
            }
        }
    }

    /**
     * Refreshes the AttendanceController UI after updating attendance records.
     */
    private void refreshAttendanceUI() {
        AttendanceController attendanceController = ControllerRegistry.getAttendanceController();
        if (attendanceController != null) {
            Platform.runLater(attendanceController::loadAttendanceRecords);
        }
    }



    /**
     * Helper method to check if student is in the session roster.
     */
    // private boolean isInRoster(Session session, String studentId) {
    //     return session.getRoster().stream()
    //             .anyMatch(s -> s.getStudentId().equals(studentId));
    // }
}