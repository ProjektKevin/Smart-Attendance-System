package com.smartattendance.service;

import java.util.List;

import com.smartattendance.model.entity.AttendanceRecord;

public class ManualAttendanceMarker implements AttendanceMarker {

    private final AttendanceService service; 

    public ManualAttendanceMarker(AttendanceService service) {
        this.service = service;
    }

    @Override
    public void markAttendance(AttendanceRecord record) throws Exception {
        // Actual update logic
        if (record.isStatusChanged()) {
            record.setTimestamp(java.time.LocalDateTime.now());
            service.updateStatus(record);
        }
        if (record.isNoteChanged()) {
            service.updateNote(record);
        }
    }
}
