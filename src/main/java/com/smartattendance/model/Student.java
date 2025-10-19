package com.smartattendance.model;

public class Student {
  private final String studentId, name, course;

  public Student(String id, String name, String course) {
    this.studentId = id;
    this.name = name;
    this.course = course;
  }

  public String getStudentId() {
    return studentId;
  }

  public String getName() {
    return name;
  }

  public String getCourse() {
    return course;
  }
}
