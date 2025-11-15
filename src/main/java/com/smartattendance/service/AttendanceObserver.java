/*
 # Done by: Chue Wan Yan
 # Step: 7
 # Date: 13 Oct 2025
*/

package com.smartattendance.service;

/**
 * Observer interface for updating UI or logs when attendance changes.
 * Implements the Observer Design Pattern.
 */
public interface AttendanceObserver {
    // F_MA: added by felicia handling marking attendance

    // void onAttendanceMarked(AttendanceRecord record, String message);
    void onAttendanceMarked(String message);


    // void onAttendanceAutoUpdated();

    // void attendanceUpdated(AttendanceRecord record);
}
