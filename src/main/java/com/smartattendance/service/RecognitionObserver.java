package com.smartattendance.service;

import java.util.function.Consumer;

import com.smartattendance.model.entity.AttendanceRecord;

/**
 * Interface for observing recognition-related events during attendance marking.
 * Extends {@link AttendanceObserver} to include additional callbacks specific
 * to recognition and confirmation scenarios.
 * 
 * @author Chue Wan Yan
 * 
 * @version 13:23 15 Nov 2025
 */
public interface RecognitionObserver extends AttendanceObserver {
    /**
     * Called when an attendance record is intentionally skipped during recognition.
     *
     * @param message A descriptive message explaining why the attendance was skipped.
     */
    void onAttendanceSkipped(String message);

    /**
     * Called when an attendance record could not be marked, for example due to low
     * confidence or system error.
     *
     * @param record The {@link AttendanceRecord} that could not be marked.
     */
    void onAttendanceNotMarked(AttendanceRecord record);

    /**
     * Requests user confirmation asynchronously for an attendance record, typically
     * when recognition confidence is low or ambiguous.
     *
     * The implementing method should display a prompt to the user and then invoke
     * the provided {@code callback} with {@code true} if the user confirms, or
     * {@code false} if they reject or cancel.
     *
     * @param record   The {@link AttendanceRecord} requiring confirmation.
     * @param callback A {@link Consumer} that receives the user's confirmation result.
     */
    void requestUserConfirmationAsync(AttendanceRecord record, Consumer<Boolean> callback);
}

