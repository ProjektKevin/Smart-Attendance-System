package com.smartattendance.controller;

import com.smartattendance.controller.student.StudentSessionContext;
import com.smartattendance.model.User;
import com.smartattendance.repository.InMemoryUserRepository;
import com.smartattendance.service.AuthService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField; // Admin username OR Student ID
    @FXML
    private PasswordField passwordField; // Admin password OR "student123"
    @FXML
    private Label errorLabel;

    private static final String STUDENT_PASSWORD = "student123";

    private final AuthService authService = new AuthService(new InMemoryUserRepository());

    // ===== Admin login (existing) =====
    @FXML
    private void onLoginButtonClick() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();

            User user = authService.authenticate(username, password);
            if (user != null) {
                Parent mainRoot = FXMLLoader.load(getClass().getResource("/view/MainView.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(mainRoot));
                stage.setTitle("Smart Attendance - " + user.getRole());
                errorLabel.setText("");
            } else {
                errorLabel.setText("Invalid username or password.");
            }
        } catch (Exception e) {
            errorLabel.setText("Load UI failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== Student login (new) =====
    // Wire your Student Login button to onAction="#handleStudentLogin" in
    // LoginView.fxml
    @FXML
    private void handleStudentLogin() {
        String studentId = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String pwd = passwordField.getText() == null ? "" : passwordField.getText();

        if (studentId.isEmpty()) {
            errorLabel.setText("Please enter your Student ID.");
            return;
        }
        if (!STUDENT_PASSWORD.equals(pwd)) {
            errorLabel.setText("Invalid password for Student.");
            return;
        }

        try {
            // Save current student ID for the student pages
            StudentSessionContext.setCurrentStudentId(studentId);

            // Load the student portal (tabs: Face Capture, My Attendance)
            Parent studentRoot = FXMLLoader.load(getClass().getResource("/view/StudentRootView.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(studentRoot));
            stage.setTitle("Student Portal - " + studentId);
            errorLabel.setText("");
        } catch (Exception e) {
            errorLabel.setText("Unable to open student portal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
