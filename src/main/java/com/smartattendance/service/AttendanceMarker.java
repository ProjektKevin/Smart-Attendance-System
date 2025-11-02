/*
 # Done by: Chue Wan Yan
 # Step: 6
 # Date: 13 Oct 2025
*/

package com.smartattendance.service;
import com.smartattendance.model.AttendanceRecord;

/**
 * Strategy interface for marking attendance.
 * Used by both AutoMarker and ManualMarker classes.
 */
public interface AttendanceMarker {
    // void markAttendance(Session session, AttendanceRecord record, LocalDateTime currentTime, float confidence, float threshold);
    void markAttendance(AttendanceRecord record) throws Exception;


    // void registerObserver(AttendanceObserver observer);

    // void unregisterObserver(AttendanceObserver observer);
}
