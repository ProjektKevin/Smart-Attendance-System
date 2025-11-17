package com.smartattendance.controller.auth;

import java.util.Map;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.User;
import com.smartattendance.service.AuthService;
import com.smartattendance.util.security.PasswordUtil;
import com.smartattendance.util.security.log.ApplicationLogger;
import com.smartattendance.util.validation.AuthValidator;
import com.smartattendance.util.validation.ValidationResult;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for user registration view
 * Handles registration for newly invited users without password
 * Requires: username, firstName, lastName, password, confirmPassword
 * 
 * @author Thiha Swan Htet
 */
public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private Label usernameError;

    @FXML
    private TextField firstNameField;
    @FXML
    private Label firstNameError;

    @FXML
    private TextField lastNameField;
    @FXML
    private Label lastNameError;

    @FXML
    private PasswordField passwordField;
    @FXML
    private Label passwordError;

    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label confirmPasswordError;

    @FXML
    private Label errorLabel;
    @FXML
    private Label successLabel;

    @FXML
    private Button cancelButton;
    @FXML
    private Button submitButton;

    private final AuthService authService = ApplicationContext.getAuthService();
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();
    private User user;

    /**
     * Initialize controller with user to register
     *
     * @param user The user to register
     */
    public void initializeUser(User user) {
        this.user = user;
    }

    /**
     * Handle registration submission
     */
    @FXML
    private void handleSubmit() {
        // Clear all errors
        clearAllErrors();

        // Get form values
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        // Validate all fields
        ValidationResult validationResult = AuthValidator.validateRegistration(
                username, firstName, lastName, password, confirmPassword);

        if (!validationResult.isValid()) {
            displayFieldErrors(validationResult);
            return;
        }

        try {
            // Check if username already exists in database
            User duplicateUser = authService.getUserByUsername(username);

            // Reject if duplicate username
            if (duplicateUser != null) {
                usernameError.setText("Username: " + username + "already exists.");
                return;
            }

            // Hash password
            String hashedPassword = PasswordUtil.hash(password);

            /*
             * Complete registration in a transaction
             * Insert user profile and turn email verification to true
             */
            boolean registrationSuccess = authService.registerUser(
                    user.getId(),
                    username,
                    hashedPassword,
                    firstName,
                    lastName);

            if (registrationSuccess) {
                successLabel.setText("Registration successful! You can now log in.");

                // Disable form for 2 seconds, then return to login
                submitButton.setDisable(true);
                cancelButton.setDisable(true);

                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(2));
                pause.setOnFinished(event -> returnToLogin());
                pause.play();

            } else {
                errorLabel.setText("Failed to complete registration. Please try again.");
            }

        } catch (Exception e) {
            errorLabel.setText("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle cancel button - return to login
     */
    @FXML
    private void handleCancel() {
        returnToLogin();
    }

    /**
     * Return to login screen
     */
    private void returnToLogin() {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/view/loginView.fxml"));
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Smart Attendance System - Login");
        } catch (Exception e) {
            errorLabel.setText("Error returning to login: " + e.getMessage());
            appLogger.error("Error Redirecting Login", e);
            return;
        }
    }

    /**
     * Clear all error messages
     * Binding automatically handles visibility based on text content
     */
    private void clearAllErrors() {
        usernameError.setText("");
        firstNameError.setText("");
        lastNameError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        errorLabel.setText("");
        successLabel.setText("");
    }

    /**
     * Display field-specific validation errors
     * Binding automatically handles visibility based on text content
     */
    private void displayFieldErrors(ValidationResult validationResult) {
        clearAllErrors();

        Map<String, String> fieldErrors = validationResult.getAllFieldErrors();

        // Display username error
        if (fieldErrors.containsKey("username")) {
            usernameError.setText(fieldErrors.get("username"));
        }

        // Display first name error
        if (fieldErrors.containsKey("firstName")) {
            firstNameError.setText(fieldErrors.get("firstName"));
        }

        // Display last name error
        if (fieldErrors.containsKey("lastName")) {
            lastNameError.setText(fieldErrors.get("lastName"));
        }

        // Display password error
        if (fieldErrors.containsKey("newPassword")) {
            passwordError.setText(fieldErrors.get("newPassword"));
        }

        // Display confirm password error
        if (fieldErrors.containsKey("confirmPassword")) {
            confirmPasswordError.setText(fieldErrors.get("confirmPassword"));
        }
    }
}
