package com.smartattendance.model;

public class Student {
  private final String studentId, name, group, courseId;

  public Student(String id, String name, String group, String courseId) {
    this.studentId = id;
    this.name = name;
    this.group = group;
    this.courseId = courseId;
  }

  public String getStudentId() {
    return studentId;
  }

  public String getName() {
    return name;
  }

  public String getGroup() {
    return group;
  }

  public String getCourseId() {
    return courseId;
  }
}
