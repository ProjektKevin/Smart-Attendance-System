/*
 # Done by: Chue Wan Yan
 # Step: 6
 # Date: 13 Oct 2025
*/

package com.smartattendance.service;
import java.util.List;

import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.service.AttendanceObserver;

/**
 * Strategy interface for marking attendance.
 * Used by both AutoMarker and ManualMarker classes.
 * 
 * @author Chue Wan Yan 
 * 
 * @version 13:25 12 Nov 2025
 */
public interface AttendanceMarker {
    /**
     * Marks attendance for the given attendance record according to the
     * implementation’s marking strategy.
     *
     * @param record the attendance record to be updated
     * @throws Exception if the attendance marking process fails
     */
    void markAttendance( AttendanceRecord record) throws Exception;

    // void markAttendance(Session session, AttendanceRecord record, LocalDateTime currentTime, float confidence, float threshold);

    // void registerObserver(AttendanceObserver observer);

    // void unregisterObserver(AttendanceObserver observer);
}
