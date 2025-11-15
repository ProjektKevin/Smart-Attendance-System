package com.smartattendance.model.entity;

public class Student {

    private int studentId;
    private String name;
    private String course;
    private FaceData faceData;

    /**
     * Custom constructor which sets student profile
     *
     * @param studentId The id of the profile
     * @param name      The name of the student
     * @param course    The course the student is enrolled in
     */
    public Student(int studentId, String name, String course) {
        this.studentId = studentId;
        this.name = name;
        this.course = course;
    }

    /**
     * Custom constructor which sets student profile
     *
     * @param studentId The id of the profile
     * @param name      The name of the student
     */
    public Student(int studentId, String name) {
        this.studentId = studentId;
        this.name = name;
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

    public FaceData getFaceData() {
        return faceData;
    }

    public void setFaceData(FaceData faceData) {
        this.faceData = faceData;
    }
}
