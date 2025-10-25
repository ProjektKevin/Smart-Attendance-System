package com.smartattendance.controller;

import com.smartattendance.model.AttendanceRecord;
import com.smartattendance.service.RecognitionService;
import com.smartattendance.util.AttendanceObserver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

public class LiveRecognitionController implements AttendanceObserver {
  @FXML
  private ImageView cameraView;
  @FXML
  private Label statusLabel;
  private final RecognitionService recognitionService = new RecognitionService();

  @FXML
  private void initialize() {
    recognitionService.getAttendanceService().addObserver(this);
    statusLabel.setText("Idle");
  }

  @FXML
  private void onStartCamera() {
    statusLabel.setText("Starting (stub)...");
    recognitionService.startRecognition();
  }

  @FXML
  private void onStopCamera() {
    statusLabel.setText("Stopped (stub).");
    recognitionService.stopRecognition();
  }

  @Override
  public void onAttendanceMarked(AttendanceRecord r) {
    Platform.runLater(() -> statusLabel.setText("Marked: " + r.getStudent().getName() + " (" + r.getStatus() + ")"));
  }
}
