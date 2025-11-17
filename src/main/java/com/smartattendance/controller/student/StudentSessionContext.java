package com.smartattendance.controller.student;

/**
 * Simple static context holder for the currently active student ID
 * on the student-facing screens.
 *
 * <p>
 * This class provides a very lightweight way to share the selected
 * student's identifier across different controllers in the
 * {@code com.smartattendance.controller.student} package.
 * It is intentionally minimal and uses static accessors instead of
 * dependency injection to keep the student UI wiring simple.
 * </p>
 *
 * <p>
 * Note: This class is <strong>not</strong> thread-safe and is intended
 * for use on the JavaFX application thread only.
 * </p>
 *
 * @author Ernest Lun
 */
public final class StudentSessionContext {

    /**
     * ID of the currently selected/active student in the student UI flow.
     * <p>
     * This is typically the database or application-level identifier
     * for the student whose attendance is being viewed.
     * </p>
     */
    private static String currentStudentId;

    /**
     * Private constructor to prevent instantiation.
     * <p>
     * All access should go through the static getter and setter.
     * </p>
     */
    private StudentSessionContext() {
        // Utility class; no instances allowed.
    }

    /**
     * Returns the ID of the currently active student.
     *
     * @return the current student ID, or {@code null} if none has been set
     */
    public static String getCurrentStudentId() {
        return currentStudentId;
    }

    /**
     * Updates the ID of the currently active student.
     *
     * @param id the new current student ID (may be {@code null} to clear)
     */
    public static void setCurrentStudentId(String id) {
        currentStudentId = id;
    }
}
