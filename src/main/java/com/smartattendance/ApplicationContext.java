package com.smartattendance;

import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.StudentService;

public final class ApplicationContext {

    private static boolean initialized = false;

    private static StudentService studentService;
    private static AttendanceService attendanceService;

    /**
     * Initialize the application context. Must be called once at application
     * startup.
     * This method initializes all services.
     * 
     * @throws IllegalStateException if already initialized
     */
    public static void initialize() {
        if (initialized) {
            throw new IllegalStateException("ApplicationContext already initialized");
        }

        // chore(): Add config loading here after implementation

        // chore(): Add database initialization here after implementation

        // chore(): Add repositories here after implementation

        // Initialize services
        studentService = new StudentService();
        attendanceService = new AttendanceService();

        initialized = true;
    }

    /**
     * Get the StudentService instance.
     *
     * @return StudentService
     * @throws IllegalStateException if not initialized
     */
    public static StudentService getStudentService() {
        checkInitialized();
        return studentService;
    }

    /**
     * Get the AttendanceService instance.
     *
     * @return AttendanceService
     * @throws IllegalStateException if not initialized
     */
    public static AttendanceService getAttendanceService() {
        checkInitialized();
        return attendanceService;
    }

    /**
     * Check if ApplicationContext has been initialized.
     *
     * @throws IllegalStateException if not initialized
     */
    private static void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                    "ApplicationContext not initialized.");
        }
    }

    /**
     * Shutdown and cleanup resources.
     */
    public static void shutdown() {
        if (!initialized) {
            return;
        }

        // chore(): Add cleanup logic here (close database connections, release resources, etc.)
        initialized = false;
    }
}
