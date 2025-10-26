package com.smartattendance.model;

public class Image {
  private int imageId;
  private String studentId;
  private String imagePath;
  private String captureData;


  public Image(int imageId, String studentId, String imagePath, String captureData) {
    this.imageId = imageId;
    this.studentId = studentId;
    this.imagePath = imagePath;
    this.captureData = captureData;
  }

  public int getImageId() {
    return imageId;
  }

  public void setImageId(int imageId) {
    this.imageId = imageId;
  }

  public String getStudentId() {
    return studentId;
  }

  public void setStudentId(String studentId) {
    this.studentId = studentId;
  }

  public String getImagePath() {
    return imagePath;
  }

  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

  public String getCaptureData() {
    return captureData;
  }

  public void setCaptureData(String captureData) {
    this.captureData = captureData;
  }
}
