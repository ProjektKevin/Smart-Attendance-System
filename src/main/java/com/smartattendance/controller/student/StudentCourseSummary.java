package com.smartattendance.controller.student;

public class StudentCourseSummary {

    private final String course;
    private final int attended;
    private final int total;
    private final double ratio;

    public StudentCourseSummary(String course, int attended, int total, double ratio) {
        this.course = course;
        this.attended = attended;
        this.total = total;
        this.ratio = ratio;
    }

    public String getCourse() {
        return course;
    }

    public int getAttended() {
        return attended;
    }

    public int getTotal() {
        return total;
    }

    public Double getRatio() {
        return ratio;
    }
}
