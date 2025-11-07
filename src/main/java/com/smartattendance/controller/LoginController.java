package com.smartattendance.controller;

import java.util.Map;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.AuthSession;
import com.smartattendance.model.entity.User;
import com.smartattendance.model.enums.Role;
import com.smartattendance.service.AuthService;
import com.smartattendance.util.security.PasswordUtil;
import com.smartattendance.util.validation.AuthValidator;
import com.smartattendance.util.validation.ProfileValidator;
import com.smartattendance.util.validation.ValidationResult;

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
    @FXML
    private Label usernameError;
    @FXML
    private Label passwordError;
    @FXML
    private javafx.scene.control.Button forgetPasswordButton;

    private final AuthService authService = ApplicationContext.getAuthService();
    private final AuthSession session = ApplicationContext.getAuthSession();

    // ===== Login =====
    @FXML
    private void handleLogin() {
        String userNameInput = usernameField.getText();
        String passwordInput = passwordField.getText();

        // Validate all fields using ProfileValidator
        // This returns a ValidationResult with field errors
        ValidationResult validationResult = AuthValidator.validateLogin(userNameInput, passwordInput);

        // If validation failed, display errors under each field
        if (!validationResult.isValid()) {
            displayLoginFieldErrors(validationResult);
            return;
        }

        // All validations passed - clear field errors
        clearLoginFieldErrors();

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
            Role role = user.getRole();

            // Save user in Application Context BEFORE loading UI
            session.login(user);

            // Load UI based on the role
            if (role == Role.ADMIN) {
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

    /**
     * Handle "Forget Password" button click.
     * Shows a message to user about password recovery.
     */
    @FXML
    private void handleForgetPassword() {
        /*
         * chore(Urgent), Harry
         * forgot password
         * Let user enter the email
         * Search the email
         * 
         * If email is not found > Show interface for registration
         * If email is found > Check email verification status
         * 
         * Email Verified > false
         * Frontend
         * Show interface to enter a random string
         * 
         * Backend
         * generate a random string
         * add expiration, service name (verify_user, forget_password),token to the
         * database.
         * Send email to the user
         * 
         * Validate token
         * Let user enter the string
         * validate it against expiration
         * 
         * If valid > Shows registration
         * If invalid > allow to let user send email again
         */
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Forget Password");
        alert.setHeaderText("Password Recovery");
        alert.setContentText("Please contact your administrator to reset your password.");
        alert.showAndWait();
    }

    /**
     * Clear all field-specific error messages.
     * Hides all error labels under input fields.
     */
    private void clearLoginFieldErrors() {
        usernameError.setText("");
        passwordError.setText("");
    }

    /**
     * Display field-specific validation errors under each input field.
     * Maps ValidationResult errors to their corresponding error labels.
     * Works like Zod in React - each field shows its own error.
     *
     * @param validationResult the ValidationResult from AuthValidator
     */
    private void displayLoginFieldErrors(ValidationResult validationResult) {
        // Clear all errors first
        clearLoginFieldErrors();

        // Get the error map (field name -> error message)
        Map<String, String> fieldErrors = validationResult.getAllFieldErrors();

        // Display error under username field if it exists
        if (fieldErrors.containsKey("username")) {
            usernameError.setText(fieldErrors.get("username"));
        }

        // Display error under password field if it exists
        if (fieldErrors.containsKey("password")) {
            passwordError.setText(fieldErrors.get("password"));
        }
    }
}
