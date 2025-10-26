package com.smartattendance.controller;

import com.smartattendance.model.Session;
import com.smartattendance.service.SessionService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SessionFormController {

    @FXML private TextField txtCourse;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtStart;
    @FXML private TextField txtEnd;
    @FXML private TextField txtLocation;
    @FXML private TextField txtLate;

    private Session newSession;

    public Session getNewSession() {
        return newSession;
    }

    private boolean isSessionEndTimeInPast(LocalDate date, LocalTime endTime) {
        LocalDateTime sessionEndDateTime = LocalDateTime.of(date, endTime);
        LocalDateTime currentDateTime = LocalDateTime.now();
        return sessionEndDateTime.isBefore(currentDateTime);
    }

    @FXML
    private void onCancel() {
        ((Stage) txtCourse.getScene().getWindow()).close();
    }

    @FXML
    private void onCreate() {
        try {
            String course = txtCourse.getText();
            LocalDate date = datePicker.getValue();
            LocalTime start = LocalTime.parse(txtStart.getText());
            LocalTime end = LocalTime.parse(txtEnd.getText());
            String loc = txtLocation.getText();
            int late = Integer.parseInt(txtLate.getText());

            // Validate that the session end time is not in the past
            if (isSessionEndTimeInPast(date, end)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Invalid Session Time");
                alert.setContentText("Cannot create a session that has already ended. Please select a future date and time.");
                alert.showAndWait();
                return;
            }

            // Validate that end time is after start time
            if (end.isBefore(start) || end.equals(start)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Invalid Time Range");
                alert.setContentText("End time must be after start time.");
                alert.showAndWait();
                return;
            }

            newSession = new SessionService().createSession(course.toUpperCase(), date, start, end, loc, late);
            ((Stage) txtCourse.getScene().getWindow()).close();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Invalid input");
            alert.setContentText("Please check your fields: " + e.getMessage());
            alert.showAndWait();
        }
    }
}