package com.smartattendance.controller;

import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.service.RecognitionService;
import com.smartattendance.util.AttendanceObserver;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
        Platform.runLater(() -> statusLabel.setText("Marked: " + r.getStudent().getUserName() + " (" + r.getStatus() + ")"));
    }

    // F_MA: modified by felicia handling marking attendance
    public void onAttendanceNotMarked(AttendanceRecord r) {
        Platform.runLater(() -> statusLabel.setText("Error marking attendance for " + r.getStudent().getUserName() + ". Please try again."));
    }
}
