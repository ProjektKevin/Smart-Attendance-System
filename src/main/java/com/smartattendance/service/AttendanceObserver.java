/*
 # Done by: Chue Wan Yan
 # Step: 7
 # Date: 13 Oct 2025
*/

package com.smartattendance.service;
import com.smartattendance.model.AttendanceRecord;

/**
 * Observer interface for updating UI or logs when attendance changes.
 * Implements the Observer Design Pattern.
 */
public interface AttendanceObserver {
    void attendanceUpdated(AttendanceRecord record);
}
