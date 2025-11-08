package com.smartattendance.util;

import java.util.List;

import com.smartattendance.model.entity.AttendanceRecord;

public interface AttendanceObserver {

    void onAttendanceMarked(AttendanceRecord record);

    // F_MA: added by felicia handling marking attendance
    // New method for bulk updates (like auto-updating pending to absent)
    // use default so existing code that implements AttendanceObserver won’t break.
    default void onAttendanceAutoUpdated(List<AttendanceRecord> updatedRecords) {
    }

    void onAttendanceAutoUpdated();
}
