package com.smartattendance.controller.student;

public final class StudentSessionContext {
    private static String currentStudentId;

    private StudentSessionContext() {
    }

    public static String getCurrentStudentId() {
        return currentStudentId;
    }

    public static void setCurrentStudentId(String id) {
        currentStudentId = id;
    }
}
