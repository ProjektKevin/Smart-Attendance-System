package com.smartattendance.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.smartattendance.model.entity.Course;
import com.smartattendance.model.entity.Session;
import com.smartattendance.service.CourseService;
import com.smartattendance.service.SessionService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SessionFormController {

    @FXML private ComboBox<String> cmbCourse;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtStart;
    @FXML private TextField txtEnd;
    @FXML private TextField txtLocation;
    @FXML private TextField txtLate;

    private Session newSession;
    private final CourseService service = new CourseService();

    @FXML
    public void initialize() {
        for (Course course : service.getCourses()) {
            cmbCourse.getItems().add(course.getCode());
        }
    }

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
        ((Stage) cmbCourse.getScene().getWindow()).close();
    }

    @FXML
    private void onCreate() {
        try {
            String course = cmbCourse.getValue();
            if (course == null || course.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Missing Course");
                alert.setContentText("Please select a course.");
                alert.showAndWait();
                return;
            }

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

            newSession = new SessionService().createSession(course, date, start, end, loc, late);
            ((Stage) cmbCourse.getScene().getWindow()).close();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Invalid input");
            alert.setContentText("Please check your fields: " + e.getMessage());
            alert.showAndWait();
        }
    }
}