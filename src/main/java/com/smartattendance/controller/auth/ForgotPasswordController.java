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
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for forgot password view
 * Handles password reset for users who forgot password
 * Requires: newPassword, confirmPassword
 * 
 * @author Thiha Swan Htet
 */
public class ForgotPasswordController {

    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Label newPasswordError;

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
     * Initialize controller with user to reset password
     *
     * @param user The user to reset password for
     */
    public void initializeUser(User user) {
        this.user = user;
    }

    /**
     * Handle forgot password submission
     */
    @FXML
    private void handleSubmit() {
        // Clear all errors
        clearAllErrors();

        // Get form values
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate all fields
        ValidationResult validationResult = AuthValidator.validatePasswordReset(newPassword, confirmPassword);

        if (!validationResult.isValid()) {
            displayFieldErrors(validationResult);
            return;
        }

        // All validations passed - attempt to update password
        try {
            String hashedPassword = PasswordUtil.hash(newPassword);

            // Update password in database
            boolean updateSuccess = authService.resetPassword(user.getId(), hashedPassword);

            if (updateSuccess) {
                successLabel.setText("Password updated successfully!");

                // Disable form for 2 seconds, then return to login
                submitButton.setDisable(true);
                cancelButton.setDisable(true);

                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(2));
                pause.setOnFinished(event -> returnToLogin());
                pause.play();

            } else {
                errorLabel.setText("Failed to update password. Please try again.");
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
        }
    }

    /**
     * Clear all error messages
     * Binding automatically handles visibility based on text content
     */
    private void clearAllErrors() {
        newPasswordError.setText("");
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

        // Display new password error
        if (fieldErrors.containsKey("newPassword")) {
            newPasswordError.setText(fieldErrors.get("newPassword"));
        }

        // Display confirm password error
        if (fieldErrors.containsKey("confirmPassword")) {
            confirmPasswordError.setText(fieldErrors.get("confirmPassword"));
        }
    }
}
