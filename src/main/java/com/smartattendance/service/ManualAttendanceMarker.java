package com.smartattendance.service;
import java.time.LocalDateTime;

import com.smartattendance.model.AttendanceRecord;
import com.smartattendance.model.MarkMethod;
import com.smartattendance.model.Session;

public class ManualAttendanceMarker implements AttendanceMarker {
    @Override
    public void markAttendance(Session session, AttendanceRecord record, LocalDateTime currentTime){
        record.mark(record.getStatus(), currentTime, MarkMethod.MANUAL, "");
        System.out.println("Marked (MANUAL) " + record.getStudent().getName() + " manually.");
    }
}