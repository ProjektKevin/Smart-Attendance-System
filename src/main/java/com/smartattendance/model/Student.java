package com.smartattendance.model;

public class Student {
  private String studentId;
  private String userName;

  // this might become course object when implemented
  // chore(): Link back to course id if done
  private String group;

  /**
   * Custom constructor which sets student profile
   * 
   * @param studentId The id of the profile
   * @param user      The user object to set authentication
   * @param group     The course the student is enrolled in
   */
  public Student(String studentId, String userName, String group) {
    this.studentId = studentId;
    this.userName = userName;
    this.group = group;
  }

  public String getStudentId() {
    return studentId;
  }

  public void setStudentId(String studentId) {
    this.studentId = studentId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }
}
