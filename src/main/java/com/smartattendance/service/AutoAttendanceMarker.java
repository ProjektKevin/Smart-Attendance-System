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

    @Override
    public void markAttendance(Session session, AttendanceRecord record, LocalDateTime currentTime, float confidence, float threshold) {
        long minutesLate = Duration.between(session.getStartTime(), currentTime).toMinutes();
        AttendanceStatus status = (minutesLate > lateThresholdMinutes)
                ? AttendanceStatus.LATE
                : AttendanceStatus.PRESENT;

        record.mark(status, currentTime, MarkMethod.AUTO, "");
        System.out.println("Marked (AUTO)" + record.getStudent().getName() + " as " + status);
    }
}
