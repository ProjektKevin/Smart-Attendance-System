package com.smartattendance.service.recognition;

import com.smartattendance.model.entity.Student;

public class RecognitionResult {
  private Student matchedStudent;
  private double confidenceScore;
  private boolean isMatch;

  // ----- Constructors -----
  // constrcutor for successful match
  public RecognitionResult(Student matchedStudent, double confidenceScore) {
    this.matchedStudent = matchedStudent;
    this.confidenceScore = confidenceScore;
    this.isMatch = matchedStudent != null;
  }

  // constructor for no match found
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
