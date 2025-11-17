package com.smartattendance.service.recognition;

import com.smartattendance.model.entity.Student;

/**
 * Recognition Result
 * Encapsulates the result of a face recognition operation
 * Contains the matched student, confidence score, and match status
 * 
 * @author Min Thet Khine
 */

public class RecognitionResult {
  private Student matchedStudent;
  private double confidenceScore;
  private boolean isMatch;

  // ----- Constructors -----
  /**
   * Constructor with default confidence threshold
   * Initializes the OpenFace recognizer with face processing service
   * 
   * @param faceProcessingService Service for preprocessing face images
   */
  public RecognitionResult(Student matchedStudent, double confidenceScore) {
    this.matchedStudent = matchedStudent;
    this.confidenceScore = confidenceScore;
    this.isMatch = matchedStudent != null;
  }

  /**
   * Constructor with custom confidence threshold
   * 
   * @param faceProcessingService Service for preprocessing face images
   * @param confidenceThreshold   Custom threshold for recognition confidence
   */
  public RecognitionResult() {
    this.matchedStudent = null;
    this.confidenceScore = 0.0;
    this.isMatch = false;
  }

  // ----- Getters -----
  public Student getMatchedStudent() {
    return matchedStudent;
  }

  public double getConfidenceScore() {
    return confidenceScore;
  }

  public boolean isMatch() {
    return isMatch;
  }

  // Other functions
  public boolean requiresConfirmation() {
    return confidenceScore >= 50.0 && confidenceScore < 70.0;
  }
}
