/*
 # Done by: Chue Wan Yan
 # Step: 6
 # Date: 13 Oct 2025
*/

package com.smartattendance.service;

/**
 * Strategy interface for marking attendance.
 * Used by both AutoMarker and ManualMarker classes.
 */
public interface AttendanceMarker {
    void markAttendance(String studentId, float confidence);

    void registerObserver(AttendanceObserver observer);

    void unregisterObserver(AttendanceObserver observer);
}
