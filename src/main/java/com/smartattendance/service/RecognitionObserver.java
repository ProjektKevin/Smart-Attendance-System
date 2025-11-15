package com.smartattendance.service;

import java.util.function.Consumer;

import com.smartattendance.model.entity.AttendanceRecord;

public interface RecognitionObserver extends AttendanceObserver {
    void onAttendanceSkipped(String message);
    void onAttendanceNotMarked(AttendanceRecord record);
    void requestUserConfirmationAsync(AttendanceRecord record, Consumer<Boolean> callback);
}

