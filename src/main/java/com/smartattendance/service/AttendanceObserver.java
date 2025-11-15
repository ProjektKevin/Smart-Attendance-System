package com.smartattendance.service;

/**
 * Observer interface for updating UI or logs when attendance changes.
 * Implements the Observer Design Pattern.
 * 
 * @author Chue Wan Yan 
 * 
 * @version 18:45 15 Nov 2025
 */
public interface AttendanceObserver {
    /**
     * Called when an attendance record has been marked. 
     * Inherited by RecognitionObserver.
     *
     * @param message a message describing the attendance update event
     */
    void onAttendanceMarked(String message);

    // void onAttendanceAutoUpdated();

    // void attendanceUpdated(AttendanceRecord record);
}
