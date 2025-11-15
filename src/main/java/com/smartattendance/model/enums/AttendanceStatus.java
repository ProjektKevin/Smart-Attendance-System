package com.smartattendance.model.enums;

/**
 * Represents the possible attendance states for a student during a session.
 *
 * This enum is used to indicate whether a student's attendance is still
 * unmarked ({@link #PENDING}), confirmed as {@link #PRESENT}, marked as
 * {@link #ABSENT}, or recorded as {@link #LATE}.
 * 
 * @author Chue Wan Yan
 *
 * @version 15:41 07 Nov 2025
 */
public enum AttendanceStatus {
    PENDING, // Attendance has not been marked yet (default)
    PRESENT, // Student attended the session
    ABSENT, // Student was absent from the session
    LATE // Student attended the session but arrived late
}
