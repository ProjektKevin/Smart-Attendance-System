package com.smartattendance.util.security.log;

import java.util.logging.Level;

/**
 * AttendanceLogger: Logger for attendance-related events
 * Logs all attendance marking, student present/absent records, etc.
 *
 * Extends BaseLogger to inherit common logging functionality
 *
 * Usage: AttendanceLogger.getInstance().info("Student marked present");
 *
 * @author Thiha Swan Htet
 */
public class AttendanceLogger extends BaseLogger {
    private static final AttendanceLogger instance = new AttendanceLogger();

    /**
     * Private constructor to prevent direct instantiation
     * Initializes logger with attendance-specific configuration
     */
    private AttendanceLogger() {
        super("AttendanceLog");
        // Setup logger: attendance.log file + console output at INFO level
        setupLogger("attendance.log", Level.INFO);
    }

    /**
     * Get the instance of AttendanceLogger
     *
     * @return the AttendanceLogger instance
     */
    public static AttendanceLogger getInstance() {
        return instance;
    }
}
