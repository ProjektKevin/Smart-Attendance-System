/*
 # Done by: Chue Wan Yan
 # Step: 2
 # Date: 13 Oct 2025
 */
package com.smartattendance.service;

import java.time.Duration;
import java.time.LocalDateTime;

import com.smartattendance.model.AttendanceRecord;
import com.smartattendance.model.AttendanceStatus;
import com.smartattendance.model.Session;
import com.smartattendance.model.Student;
import com.smartattendance.repository.AttendanceRecordRepository;
import com.smartattendance.repository.SessionRepository;


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
            String studentId = student.getStudentId();
            Session session = record.getSession();
            int sessionId = session.getSessionId();
            AttendanceRecordRepository attendanceRecordRepo = record.getAttendanceRecordRepo();
            SessionRepository sessionRepo = attendanceRecordRepo.getSessionRepo();
            LocalDateTime lastSeen = attendanceRecordRepo.findById(studentId, sessionId).getLastSeen();
            long diffInSeconds = Duration.between(lastSeen, record.getTimestamp()).getSeconds();
            long diffInMinutes = Duration.between(record.getTimestamp(), session.getStartTime()).toMinutes();

            // if the student is not marked as present before (still in default ABSENT status), update the student attendance
            if (attendanceRecordRepo.findById(studentId, sessionId).getStatus() == AttendanceStatus.ABSENT) {

                // if the student is late, set attendance status to LATE before update
                AttendanceStatus status = (diffInMinutes > sessionRepo.findById(sessionId).getLateThresholdMinutes())
                        ? AttendanceStatus.LATE
                        : AttendanceStatus.PRESENT;
                record.setStatus(status);

                // if (diffInMinutes > record.getSession().getLateThresholdMinutes()) {
                //     record.setStatus(AttendanceStatus.LATE);
                // }

                attendanceRecordRepo.update(record);
            } else {
                // if still in cooldown time, too soon to update lastSeen time
                if (diffInSeconds < 30) {
                    return;
                } 
                
                record.getAttendanceRecordRepo().updateLastSeen(record);
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
            throw new Exception("Failed to save attendance record", e);
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
