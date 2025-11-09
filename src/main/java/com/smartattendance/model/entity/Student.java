package com.smartattendance.model.entity;

public class Student {
  private String studentId;
  private String userName;

  // this might become course object when implemented
  // chore(): Link back to course id if done
  private String course;

  private FaceData faceData;

  /**
   * Custom constructor which sets student profile
   * 
   * @param studentId The id of the profile
   * @param user      The user object to set authentication
   * @param course    The course the student is enrolled in
   */
  public Student(String studentId, String userName, String course) {
    this.studentId = studentId;
    this.userName = userName;
    this.course = course;
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
