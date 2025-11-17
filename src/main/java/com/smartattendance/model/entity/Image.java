package com.smartattendance.model.entity;

/**
 * Image
 * Represents a face image record in the database
 * Links student IDs to their corresponding face image file paths
 * 
 * @author Min Thet Khine
 */

public class Image {
  private int imageId;
  private String studentId;
  private String imagePath;

  /**
   * Constructor
   * Creates an image record with all required fields
   * 
   * @param imageId   Unique identifier for the image
   * @param studentId The ID of the student this image belongs to
   * @param imagePath File path where the image is stored
   */
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
