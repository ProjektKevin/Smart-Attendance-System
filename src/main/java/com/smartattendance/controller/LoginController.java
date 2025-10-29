package com.smartattendance.controller;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.AuthSession;
import com.smartattendance.model.User;
import com.smartattendance.service.AuthService;
import com.smartattendance.util.security.PasswordUtil;

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
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final AuthService authService = ApplicationContext.getAuthService();
    private final AuthSession session = ApplicationContext.getAuthSession();

    // ===== Login =====
    @FXML
    private void handleLogin() {
        String userNameInput = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String passwordInput = passwordField.getText() == null ? "" : passwordField.getText();

        if (userNameInput.isEmpty() || passwordInput.isEmpty()) {
            errorLabel.setText("Please enter your credentials.");
            return;
        }

        try {
            // Get user details
            User user = authService.authenticate(userNameInput);

            if (user == null) {
                errorLabel.setText("Invalid credentials");
                return;
            }

            if (!PasswordUtil.matches(passwordInput, user.getPasswordHash())) {
                errorLabel.setText("Invalid credentials");
                return;
            }

            // Get user role
            String role = user.getRole();

            // Save user in Application Context BEFORE loading UI
            session.login(user);

            // Load UI based on the role
            if (role.equals("ADMIN")) {
                Parent mainRoot = FXMLLoader.load(getClass().getResource("/view/MainView.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(mainRoot));
                stage.setTitle("Admin System");
                errorLabel.setText("");
            }

            else {
                Parent studentRoot = FXMLLoader.load(getClass().getResource("/view/StudentRootView.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(studentRoot));
                stage.setTitle("Student Portal");
                errorLabel.setText("");
            }

        } catch (Exception e) {
            errorLabel.setText("Unable to open student portal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
