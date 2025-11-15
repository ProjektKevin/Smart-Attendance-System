package com.smartattendance.service;

import java.util.List;

import com.smartattendance.model.entity.AttendanceRecord;

/**
 * Marks attendance manually by a user action.
 * 
 * This class implements the {@link AttendanceMarker} interface and handles
 * updating status and note of an {@link AttendanceRecord} when changes
 * are detected.
 * 
 * @author Chue Wan Yan 
 * 
 * @version 13:23 15 Nov 2025
 */
public class ManualAttendanceMarker implements AttendanceMarker {

    private final AttendanceService service; // shared AttendanceService to performing updates

    /**
     * Constructs a ManualAttendanceMarker with the given attendance service.
     *
     * @param service the {@link AttendanceService} used to update attendance records
     */
    public ManualAttendanceMarker(AttendanceService service) {
        this.service = service;
    }

    /**
     * Marks the given attendance record manually.
     * 
     * If the status of the record has changed, it updates the marking timestamp and
     * calls {@link AttendanceService#updateStatus}. If the note has changed,
     * it calls {@link AttendanceService#updateNote}.
     * 
     *
     * @param record the {@link AttendanceRecord} to mark manually
     * @throws Exception if an error occurs during the update
     */
    @Override
    public void markAttendance(AttendanceRecord record) throws Exception {
        if (record.isStatusChanged()) {
            record.setTimestamp(java.time.LocalDateTime.now());
            service.updateStatus(record);
        }
        if (record.isNoteChanged()) {
            service.updateNote(record);
        }
    }
}
