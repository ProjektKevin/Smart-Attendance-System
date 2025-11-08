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
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.AttendanceStatus;
import com.smartattendance.model.entity.MarkMethod;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.repository.AttendanceRecordRepository;
import com.smartattendance.repository.SessionRepository;
import com.smartattendance.util.ControllerRegistry;

import javafx.application.Platform;


/**
 * Automatically marks attendance using face recognition confidence values.
 */
// public class AutoAttendanceMarker implements AttendanceMarker {

//     private final int lateThresholdMinutes = 15;
//     private final int cooldownSeconds = 30;

//     @Override
//     public void markAttendance(Session session, AttendanceRecord record, LocalDateTime currentTime) {
//         // if (confidence < confidenceThreshold) {
//         //     continue;
//         // }

//         try {
//             if (!isInRoster(session, record.getStudent().getStudentId())) {
//                 return;
//             }

//             if (record.getLastSeen() == null) {
//                 long minutesLate = Duration.between(session.getStartTime(), currentTime).toMinutes();
//                 AttendanceStatus status = (minutesLate > lateThresholdMinutes)
//                         ? AttendanceStatus.LATE
//                         : AttendanceStatus.PRESENT;

//                 record.mark(status, currentTime, MarkMethod.AUTO, "");
//                 System.out.println("Marked (AUTO)" + record.getStudent().getName() + " as " + status);
//             } else {
//                 if (Duration.between(currentTime, record.getLastSeen()).getSeconds() < cooldownSeconds) {
//                     record.setLastSeen(currentTime);
//                 } else {
//                     record.setLastSeen(currentTime);
//                 }
//             }
//         } catch (NullPointerException e) {
//             System.err.println("Error marking attendance: " + e.getMessage());
//         } catch (Exception e) {
//             System.err.println("Error marking attendance: " + e.getMessage());
//         }

//     }

public class AutoAttendanceMarker implements AttendanceMarker {

    // private final int lateThresholdMinutes = 15;
    // private final int cooldownSeconds = 30;
    private int cooldownSeconds = Integer.parseInt(Config.get("cooldown.seconds"));

    @Override
    public void markAttendance (AttendanceRecord record) throws Exception {
        // if (confidence < confidenceThreshold) {
        //     continue;
        // }

        try {
            // face recognition will only predict faces based on the roster, so no need to check again
            // if (!isInRoster(record.getSession(), record.getStudent().getStudentId())) {
            //     return;
            // }

            Student student = record.getStudent();
            // int studentId = student.getStudentId();
            Session session = record.getSession();
            // int sessionId = session.getSessionId();
            
            AttendanceRecordRepository attendanceRecordRepo = record.getAttendanceRecordRepo();
            // SessionRepository sessionRepo = attendanceRecordRepo.getSessionRepo();
            AttendanceRecord existingRecord = attendanceRecordRepo.findById(student.getStudentId(), session.getSessionId());
            
            // LocalDateTime lastSeen = existingRecord.getLastSeen();
            // long diffInSeconds = Duration.between(lastSeen, record.getTimestamp()).getSeconds();
            // long diffInMinutes = Duration.between(record.getTimestamp(), session.getStartTime()).toMinutes();
            LocalDateTime now = record.getTimestamp();

            // First time marking
            // if the student is not marked as present before (still in default PENDING status), update the student attendance
            if (existingRecord.getStatus() == AttendanceStatus.PENDING) {

                // if the student is late, set attendance status to LATE before update
                long minutesLate = Duration.between(session.getStartTime(), now).toMinutes();
                // AttendanceStatus status = (minutesLate > sessionRepo.findById(session.getSessionId()).getLateThresholdMinutes())
                AttendanceStatus status = (minutesLate > session.getLateThresholdMinutes())
                        ? AttendanceStatus.LATE
                        : AttendanceStatus.PRESENT;
                record.setStatus(status);
                record.setTimestamp(now);
                record.setMethod(MarkMethod.AUTO);

                // if (diffInMinutes > record.getSession().getLateThresholdMinutes()) {
                //     record.setStatus(AttendanceStatus.LATE);
                // }

                attendanceRecordRepo.update(record);
                System.out.println("Marked " + student.getName() + " as " + status);
            
            // student already marked
            } else if (existingRecord.getStatus() == AttendanceStatus.PRESENT || existingRecord.getStatus() == AttendanceStatus.LATE){
                long secondsSinceLastSeen = Duration.between(existingRecord.getLastSeen(), now).getSeconds();
                if (secondsSinceLastSeen >= cooldownSeconds) {
                    existingRecord.setLastSeen(now);
                    attendanceRecordRepo.updateLastSeen(record);
                    System.out.println("Updated last_seen for " + student.getName());
                } else {
                    System.out.println("Cooldown active for " + student.getName() + ", skipping re-mark.");
                }
            } else {
                System.out.println("Skipping: already marked as " + existingRecord.getStatus());
            }

            // if (record.getLastSeen() == null) {
            //     long minutesLate = Duration.between(session.getStartTime(), currentTime).toMinutes();
            //     AttendanceStatus status = (minutesLate > lateThresholdMinutes)
            //             ? AttendanceStatus.LATE
            //             : AttendanceStatus.PRESENT;

            //     record.mark(status, currentTime, MarkMethod.AUTO, "");
            //     System.out.println("Marked (AUTO)" + record.getStudent().getName() + " as " + status);
            // } else {
            //     if (Duration.between(currentTime, record.getLastSeen()).getSeconds() < cooldownSeconds) {
            //         record.setLastSeen(currentTime);
            //     } else {
            //         record.setLastSeen(currentTime);
            //     }
            // }
        } catch (Exception e) {
            throw new Exception("Failed to mark attendance: ", e);
        }

    }

    /** Core logic to mark pending attendance as ABSENT */
    public static void markPendingAttendanceAsAbsent(SessionRepository sessionRepository, AttendanceService attendanceService) {
        AttendanceRecordRepository attendanceRecordRepo = new AttendanceRecordRepository();
        List<Session> allSessions = sessionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Session sess : allSessions) {
            LocalDateTime sessionEndDateTime = LocalDateTime.of(sess.getSessionDate(), sess.getEndTime());
            boolean isClosed = "Closed".equalsIgnoreCase(sess.getStatus()) ||
                               sessionEndDateTime.isBefore(now);

            if (isClosed) {
                List<AttendanceRecord> pendingRecords = attendanceRecordRepo.findPendingAttendanceBySessionId(sess.getSessionId(), AttendanceStatus.PENDING);

                for (AttendanceRecord rec : pendingRecords) {
                    rec.setStatus(AttendanceStatus.ABSENT);
                    rec.setMethod(MarkMethod.AUTO);
                    rec.setTimestamp(now);
                    rec.setNote("Auto-marked as Absent after session closed.");
                    attendanceRecordRepo.update(rec);

                    System.out.println("Updated attendance to ABSENT for student: " + rec.getStudent().getName());
                }
            }
        }

        // Refresh the UI after updates
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
