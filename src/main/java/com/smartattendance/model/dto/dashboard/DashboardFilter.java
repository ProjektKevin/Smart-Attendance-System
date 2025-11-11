package com.smartattendance.model.dto.dashboard;

import java.time.LocalDate;

public class DashboardFilter {
    public LocalDate from;
    public LocalDate to;
    public Integer courseId;
    public Integer sessionId;

    public boolean includePresent = true;
    public boolean includeLate    = true;
    public boolean includeAbsent  = true;
    public boolean includePending = true;
}
