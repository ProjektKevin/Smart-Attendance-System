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
import com.smartattendance.model.MarkMethod;
import com.smartattendance.model.Session;

/**
 * Automatically marks attendance using face recognition confidence values.
 */
public class AutoAttendanceMarker implements AttendanceMarker {

    private final int lateThresholdMinutes = 15;
    private final int cooldownSeconds = 30;

    @Override
    public void markAttendance(Session session, AttendanceRecord record, LocalDateTime currentTime) {
        // if (confidence < confidenceThreshold) {
        //     continue;
        // }

        try {
            if (!isInRoster(session, record.getStudent().getStudentId())) {
                return;
            }

            if (record.getLastSeen() == null) {
                long minutesLate = Duration.between(session.getStartTime(), currentTime).toMinutes();
                AttendanceStatus status = (minutesLate > lateThresholdMinutes)
                        ? AttendanceStatus.LATE
                        : AttendanceStatus.PRESENT;

                record.mark(status, currentTime, MarkMethod.AUTO, "");
                System.out.println("Marked (AUTO)" + record.getStudent().getName() + " as " + status);
            } else {
                if (Duration.between(currentTime, record.getLastSeen()).getSeconds() < cooldownSeconds) {
                    record.setLastSeen(currentTime);
                } else {
                    record.setLastSeen(currentTime);
                }
            }
        } catch (NullPointerException e) {
            System.err.println("Error marking attendance: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error marking attendance: " + e.getMessage());
        }

    }

    /**
     * Helper method to check if student is in the session roster.
     */
    private boolean isInRoster(Session session, String studentId) {
        return session.getRoster().stream()
                .anyMatch(s -> s.getStudentId().equals(studentId));
    }
}
