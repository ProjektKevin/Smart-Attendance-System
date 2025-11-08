package com.smartattendance.controller.auth;

import java.time.LocalDateTime;
import java.util.Map;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.AuthSession;
import com.smartattendance.model.entity.User;
import com.smartattendance.model.entity.Verification;
import com.smartattendance.model.enums.AuthVerification;
import com.smartattendance.model.enums.Role;
import com.smartattendance.service.AuthService;
import com.smartattendance.util.EmailService;
import com.smartattendance.util.EmailSettings;
import com.smartattendance.util.EmailTemplates;
import com.smartattendance.util.security.PasswordUtil;
import com.smartattendance.util.security.RandomUtil;
import com.smartattendance.util.validation.AuthValidator;
import com.smartattendance.util.validation.ValidationResult;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.smartattendance.controller.auth.ForgotPasswordController;
import com.smartattendance.controller.auth.RegisterController;

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
     * Opens forgot password dialog, sends verification token via email, and
     * validates it.
     */
    @FXML
    private void handleForgetPassword() {
        try {
            // Open forgot password dialog
            ForgotPasswordDialog dialog = new ForgotPasswordDialog();
            dialog.show();

            if (!dialog.isSubmitted()) {
                return;
            }

            String email = dialog.getEmail();

            // Check if user exists
            User user = authService.getUserByEmail(email);

            if (user == null) {
                showAlert("User Not Found", "Please contact your administrator for invitation.");
                return;
            }

            // Generate a random token
            String token = RandomUtil.generateToken(60);

            // Forget Password Expiration: 15mins for security
            LocalDateTime forgetPasswordExpTime = LocalDateTime.now().plusMinutes(15);
            // Verification Expiration: Seven days for flexibility
            LocalDateTime verificationExpTime = LocalDateTime.now().plusDays(7);

            /*
             * Determine the identifier based on email verified
             * If verified > allow user to reset password
             * Else > let user verify email
             */
            AuthVerification verificationType = user.getIsEmailVerified()
                    ? AuthVerification.FORGOT_PASSWORD
                    : AuthVerification.VERIFICATION;

            // Assign expiration based on verification type
            LocalDateTime expirationTime = verificationType.name().equals(AuthVerification.VERIFICATION.name())
                    ? verificationExpTime
                    : forgetPasswordExpTime;

            // Create verification record
            Verification verification = new Verification(
                    verificationType,
                    token,
                    expirationTime,
                    user.getId());
            authService.createVerification(verification);

            // Send email with token
            try {
                sendVerificationEmail(user, token, verificationType);
            } catch (Exception e) {
                showAlert("Email Error", "Failed to send verification email: " + e.getMessage());
                e.printStackTrace();
                // Clean up the verification record since email wasn't sent
                authService.deleteVerification(user.getId(), verificationType);
                return;
            }

            // Email sent successfully - show verification dialog to collect token from user
            VerificationDialog verificationDialog = new VerificationDialog();
            verificationDialog.show();

            if (!verificationDialog.isSubmitted()) {
                // User cancelled verification
                authService.deleteExpiredVerifications();
                return;
            }

            String userInputToken = verificationDialog.getToken().trim();

            // Verify the token against the database
            boolean isTokenValid = authService.verifyToken(user.getId(), userInputToken, verificationType);
            System.out.println("Token Validation: " + isTokenValid);

            if (!isTokenValid) {
                showAlert("Invalid Token", "The token is invalid or has expired.");
                authService.deleteExpiredVerifications();
                return;
            }

            /*
             * Is user has password, allow to reset password
             * If not, lead to registration page
             */
            boolean hasPassword = user.getPasswordHash() != null && !user.getPasswordHash().isEmpty();

            // Clean up used verification token
            authService.deleteVerification(user.getId(), verificationType);

            // Route to appropriate screen based on password status
            if (hasPassword) {
                // User has password - show forgot password view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ForgotPasswordView.fxml"));
                Parent resetRoot = loader.load();

                // Initialize the controller with user
                ForgotPasswordController resetController = loader.getController();
                resetController.initializeUser(user);

                // Navigate to password reset screen
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(resetRoot));
                stage.setTitle("Reset Password");

            } else {
                // User has no password - show registration view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterView.fxml"));
                Parent registerRoot = loader.load();

                // Initialize the controller with user
                RegisterController registerController = loader.getController();
                registerController.initializeUser(user);

                // Navigate to registration screen
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(registerRoot));
                stage.setTitle("Complete Registration");
            }

        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send verification email with token to user
     * Throws exception if email is invalid or sending fails
     */
    private void sendVerificationEmail(User user, String token, AuthVerification type) throws Exception {
        // Validate email before attempting to send
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new Exception("User email is empty or null");
        }

        EmailSettings settings = EmailSettings.fromEnv();
        EmailService emailService = new EmailService(settings);

        String subject;
        String body;

        if (type == AuthVerification.FORGOT_PASSWORD) {
            subject = "Password Reset Request";
            body = EmailTemplates.forgotPasswordEmail(user.getUserName(), token);
        } else {
            subject = "Email Verification";
            body = EmailTemplates.verificationEmail(token);
        }

        // Send email (throws exception if fails)
        emailService.send(user.getEmail(), subject, body, null);
    }

    /**
     * Show an alert dialog to the user
     */
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
