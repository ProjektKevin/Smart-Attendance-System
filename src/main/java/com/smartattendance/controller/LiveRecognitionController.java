package com.smartattendance.controller;

import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.service.RecognitionService;
import com.smartattendance.util.AttendanceObserver;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
        Platform.runLater(() -> statusLabel.setText("Marked: " + r.getStudent().getName() + " (" + r.getStatus() + ")"));
        // Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        // alert.setTitle("Confirm Delete All");
        // alert.setHeaderText("Delete ALL Sessions");
        // alert.setContentText("WARNING: This will permanently delete ALL " + sessionList.size() + " sessions!\n\n" +
        //         "This action cannot be undone. Are you absolutely sure?");
    }

    // F_MA: modified by felicia handling marking attendance ##
    @Override
    public void onAttendanceAutoUpdated() {
        return;
    }

    // F_MA: modified by felicia handling marking attendance
    public void onAttendanceNotMarked(AttendanceRecord r) {
        Platform.runLater(() -> statusLabel.setText("Error marking attendance for " + r.getStudent().getName() + ". Please try again."));
    }
}
