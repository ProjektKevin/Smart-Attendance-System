package com.smartattendance.service.recognition;

import java.util.List;

import org.opencv.core.Mat;

import com.smartattendance.model.entity.Student;
import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * Recognizer
 * Abstract base class for face recognition algorithms
 * Defines the common interface and functionality for all recognizers
 * Subclasses must implement specific recognition and training logic
 * 
 * @author Min Thet Khine
 */

public abstract class Recognizer {
  private double confidenceThreshold;
  private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

  public Recognizer() {
    this.confidenceThreshold = 70.0; // default threshold
  }

  public Recognizer(double confidenceThreshold) {
    this.confidenceThreshold = confidenceThreshold;
  }

  // Get the current confidence threshold
  public double getConfidenceThreshold() {
    return confidenceThreshold;
  }

  /**
   * Set the confidence threshold for recognition
   * 
   * @param confidenceThreshold New threshold value (must be between 0-100)
   */
  public void setConfidenceThreshold(double confidenceThreshold) {
    if (confidenceThreshold < 0 || confidenceThreshold > 100) {
      appLogger.error("Invalid threshold: " + confidenceThreshold);
      return;
    }
    this.confidenceThreshold = confidenceThreshold;
  }

  /**
   * Recognize a face from an image
   * Abstract method to be implemented by subclasses
   * 
   * @param faceImage        The face image to recognize
   * @param enrolledStudents List of enrolled students to compare against
   * @return RecognitionResult containing the match and confidence score
   */
  public abstract RecognitionResult recognize(Mat faceImage, List<Student> enrolledStudents);

  /**
   * Train the recognition model with enrolled students' data
   * Abstract method to be implemented by subclasses
   * 
   * @param students List of students to train the model with
   */
  public abstract void train(List<Student> students);
}
