package com.smartattendance.controller.student;

import java.time.LocalDate;

public class StudentAttendanceRow {
    private final LocalDate date;
    private final String module;
    private final String status;
    private final String method;
    private final String markedAt;

    public StudentAttendanceRow(LocalDate date,
                                String module,
                                String status,
                                String method,
                                String markedAt) {
        this.date = date;
        this.module = module;
        this.status = status;
        this.method = method;
        this.markedAt = markedAt;
    }

    public LocalDate getDate()   { return date; }
    public String getModule()    { return module; }
    public String getStatus()    { return status; }
    public String getMethod()    { return method; }
    public String getMarkedAt()  { return markedAt; }
}
