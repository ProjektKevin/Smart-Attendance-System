/*
 # Done by: Chue Wan Yan
 # Step: 6
 # Date: 13 Oct 2025
*/

package com.smartattendance.service;
import java.util.List;

import com.smartattendance.model.entity.AttendanceRecord;

/**
 * Strategy interface for marking attendance.
 * Used by both AutoMarker and ManualMarker classes.
 */
public interface AttendanceMarker {
    void markAttendance(List<AttendanceObserver> observers, AttendanceRecord record) throws Exception;
}
