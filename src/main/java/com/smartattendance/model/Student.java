package com.smartattendance.model;

public class Student {
  private final String studentId, name, group;

  public Student(String id, String name, String group) {
    this.studentId = id;
    this.name = name;
    this.group = group;
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
}
