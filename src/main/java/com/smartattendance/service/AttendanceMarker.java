/*
 # Done by: Chue Wan Yan
 # Step: 6
 # Date: 13 Oct 2025
*/

package com.smartattendance.service;
import java.time.LocalDateTime;

import com.smartattendance.model.AttendanceRecord;
import com.smartattendance.model.Session;

/**
 * Strategy interface for marking attendance.
 * Used by both AutoMarker and ManualMarker classes.
 */
public interface AttendanceMarker {
    // void markAttendance(Session session, AttendanceRecord record, LocalDateTime currentTime, float confidence, float threshold);
    void markAttendance(Session session, AttendanceRecord record, LocalDateTime currentTime);


    // void registerObserver(AttendanceObserver observer);

    // void unregisterObserver(AttendanceObserver observer);
}
