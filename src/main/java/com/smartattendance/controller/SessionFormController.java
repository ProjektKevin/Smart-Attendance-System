package com.smartattendance.controller;

import com.smartattendance.model.Session;
import com.smartattendance.service.SessionService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
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