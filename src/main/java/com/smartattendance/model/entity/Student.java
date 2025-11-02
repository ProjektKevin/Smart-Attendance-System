package com.smartattendance.model.entity;

public class Student {

    private int studentId;
    private String name;

    // this might become course object when implemented
    // chore(): Link back to course id if done
    private String course;

    /**
     * Custom constructor which sets student profile
     *
     * @param studentId The id of the profile
     * @param user The user object to set authentication
     * @param course The course the student is enrolled in
     */
    public Student(int studentId, String name, String course) {
        this.studentId = studentId;
        this.name = name;
        this.course = course;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
