package com.smartattendance.model.dto.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceRecord {
    public LocalDateTime markedAt;
    public String status;
    public String method;
    public Double confidence;
    public String note;
    public Integer userId;
    public String username;
    public Integer sessionId;
    public LocalDate sessionDate;
    public LocalDateTime sessionStart;
    public Integer courseId;
    public String courseCode;
}
