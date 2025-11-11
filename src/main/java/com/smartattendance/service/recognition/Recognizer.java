package com.smartattendance.service.recognition;

import java.util.List;

import org.opencv.core.Mat;

import com.smartattendance.model.entity.Student;

public abstract class Recognizer {
  private double confidenceThreshold;

  public Recognizer() {
    this.confidenceThreshold = 70.0; // default threshold
  }

  public Recognizer(double confidenceThreshold) {
    this.confidenceThreshold = confidenceThreshold;
  }

  public double getConfidenceThreshold() {
    return confidenceThreshold;
  }

  public void setConfidenceThreshold(double confidenceThreshold) {
    if (confidenceThreshold < 0 || confidenceThreshold > 100) {
      System.err.println("Invalid threshold: " + confidenceThreshold);
      return;
    }
    this.confidenceThreshold = confidenceThreshold;
  }

  // Abstract method for recognize a face from an image
  public abstract RecognitionResult recognize(Mat faceImage, List<Student> enrolledStudents);

  // Train the recogntion model with enrolled students' data
  public abstract void train(List<Student> students);
}
