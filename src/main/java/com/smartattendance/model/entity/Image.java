package com.smartattendance.model.entity;

public class Image {
  private int imageId;
  private String studentId;
  private String imagePath;


  public Image(int imageId, String studentId, String imagePath) {
    this.imageId = imageId;
    this.studentId = studentId;
    this.imagePath = imagePath;
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
}
