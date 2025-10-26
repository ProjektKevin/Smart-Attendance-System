package com.smartattendance.service;

public class FaceRecognitionService {
  private final FaceDetectionService faceDetectionService;

  public FaceRecognitionService(FaceDetectionService faceDetectionService) {
    this.faceDetectionService = faceDetectionService;
  }
}
