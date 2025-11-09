package com.smartattendance.util;

import com.smartattendance.controller.AttendanceController;

public class ControllerRegistry {
    private static AttendanceController attendanceController;

    public static void setAttendanceController(AttendanceController controller) {
        attendanceController = controller;
    }

    public static AttendanceController getAttendanceController() {
        return attendanceController;
    }
}
