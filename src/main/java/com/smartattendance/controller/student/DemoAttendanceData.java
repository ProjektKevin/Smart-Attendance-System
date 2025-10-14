package com.smartattendance.controller.student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class DemoAttendanceData {
    private DemoAttendanceData(){}

    public static List<AttendanceRow> forStudent(String studentId) {
        List<AttendanceRow> rows = new ArrayList<>();
        rows.add(new AttendanceRow(LocalDate.now().minusDays(7), "CS102",  "Lecture 5", "Present"));
        rows.add(new AttendanceRow(LocalDate.now().minusDays(5), "IS442",  "Lab 3",     "Absent"));
        rows.add(new AttendanceRow(LocalDate.now().minusDays(3), "ST1507", "Tutorial",  "Present"));
        rows.add(new AttendanceRow(LocalDate.now().minusDays(1), "DAAA",   "Workshop",  "Present"));
        rows.add(new AttendanceRow(LocalDate.now(),              "ST1516", "Quiz 2",    "Excused"));
        return rows;
    }
}
