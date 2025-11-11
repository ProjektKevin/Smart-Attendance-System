package com.smartattendance.model.dto.dashboard;

public class DashboardTopCards {
    private final int students;
    private final int sessions;
    private final int present;

    public DashboardTopCards(int students, int sessions, int present) {
        this.students = students;
        this.sessions = sessions;
        this.present = present;
    }

    public int getStudents() {
        return students;
    }

    public int getSessions() {
        return sessions;
    }

    public int getPresent() {
        return present;
    }
}
