package com.smartattendance.controller.auth;

import java.time.LocalDateTime;
import java.util.Map;

// OpenCV imports
import org.opencv.core.Mat;

// App Context and Entity Import
import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.AuthSession;
import com.smartattendance.model.entity.User;
import com.smartattendance.model.entity.Verification;

// Enums
import com.smartattendance.model.enums.AuthVerification;
import com.smartattendance.model.enums.Role;

// Application Services
import com.smartattendance.service.AuthService;
import com.smartattendance.service.ImageService;

// Email Services
import com.smartattendance.util.EmailService;
import com.smartattendance.util.EmailSettings;
import com.smartattendance.util.EmailTemplates;

// Security Utils
import com.smartattendance.util.security.log.ApplicationLogger;
import com.smartattendance.util.security.PasswordUtil;
import com.smartattendance.util.security.RandomUtil;

// Validators
import com.smartattendance.util.validation.AuthValidator;
import com.smartattendance.util.validation.ValidationResult;

// JavaFX UI imports
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * LoginController: Handles login and forget password logic
 * Register through forget password if admin invitation is received
 * If user has password, allow password reset and login
 * If not > register: First, Last name, Password
 * Upon login, facial data is expected.
 * If not > Enroll user facial data
 * 
 * @author Thiha Swan Htet
 */
public class LoginController {

    // FXML UI Declarations
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

    // Application Services
    private final AuthService authService = ApplicationContext.getAuthService();
    private final AuthSession session = ApplicationContext.getAuthSession();
    private final ImageService imageService = ApplicationContext.getImageService();

    // Base view file folder
    private final String baseViewFolderName = "/view";

    /**
     * Handle "Login" button click.
     * Validates empty fields and direct user to the respective portal.
     * Student is a special case where facial data must be enrolled.
     * If not, direct to enrollment portal.
     */
    @FXML
    private void handleLogin() {
        String userNameInput = usernameField.getText().trim();
        String passwordInput = passwordField.getText().trim();

        // Logger to let dev know which function is running
        ApplicationLogger.getInstance().info("Running Login For the User");

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
            User user = authService.getUserByUsername(userNameInput);

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

            // Logger for more info on authentication
            ApplicationLogger.getInstance().info("User Authenticated Successfully: " + user.getId());

            /*
             * Check if facial data is enrolled on login
             * If not enrolled, call the enrollmentView file
             * If enrolled > direct dashboard
             */

            if (role == Role.STUDENT) {
                // Check if student has face data enrolled by attempting to retrieve histogram
                Mat studentHistogram = imageService.getStudentHistogram(user.getId());
                boolean hasFaceData = !studentHistogram.empty();

                ApplicationLogger.getInstance().info("Student face data status: " + (hasFaceData ? "enrolled" : "not enrolled"));

                if (!hasFaceData) {
                    // Student has no face data - redirect to enrollment
                    ApplicationLogger.getInstance().info("Redirecting student to enrollment: " + user.getUserName());
                    loadEnrollmentPortal();
                } else {
                    // Student has face data - load student dashboard
                    loadAuthPortal(role);
                }
            } else {
                // Admin user - load admin portal
                loadAuthPortal(role);
            }

            // Logger for more info on authorization
            ApplicationLogger.getInstance().info("User Authorized Successfully: " + user.getRole().name());

        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            errorLabel.setText("Login failed: " + errorMsg);
            ApplicationLogger.getInstance().error("Error during login portal loading for user: " + usernameField.getText(), e);
        }
    }

    /**
     * Handle "Forget Password" button click.
     * Opens forgot password dialog, sends verification token via email, and
     * validates it.
     * If user is verified and has password > check email, enter code, verify and
     * allow password reset
     * If user is unverified > check email, enter code, verify and register
     */
    @FXML
    private void handleForgetPassword() {
        try {
            // Open forgot password dialog
            ForgotPasswordDialog dialog = new ForgotPasswordDialog();
            dialog.show();

            // Logger to let dev know which function is running
            ApplicationLogger.getInstance().info("Running Forgot Password For the User");

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

            // Logger for user verification type
            ApplicationLogger.getInstance().info("User Auth Verification Type: " + verificationType.name());

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

            // Logger to verify verification record insertion
            ApplicationLogger.getInstance().info("Verification Queue Inserted Successfully");

            // Send email with token
            try {
                sendVerificationEmail(user, token, verificationType);
                // Logger to verify email sending
                ApplicationLogger.getInstance().info("Email Sent Successfully To: " + user.getEmail());
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                showAlert("Email Error", "Failed to send verification email: " + errorMsg);
                ApplicationLogger.getInstance().error("Error sending verification email to: " + user.getEmail(), e);
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

            // Logger to verify token validation
            ApplicationLogger.getInstance().info("Token Validation: " + isTokenValid);

            if (!isTokenValid) {
                showAlert("Invalid Token", "The token is invalid or has expired.");
                authService.deleteExpiredVerifications();
                return;
            }

            /*
             * Check user's authentication status:
             * 1. If has password → allow to reset password
             * 2. If no password → direct to registration
             */
            boolean hasPassword = user.getPasswordHash() != null && !user.getPasswordHash().isEmpty();

            // Logger for password status
            ApplicationLogger.getInstance().info("User Password Status: " + hasPassword);

            // Clean up used verification token
            authService.deleteVerification(user.getId(), verificationType);

            // Route to appropriate screen based on password status
            if (hasPassword) {
                // User has password - show forgot password view (password reset)
                loadForgetPasswordPortal(user);
                ApplicationLogger.getInstance().info("User redirected to password reset screen.");

            } else {
                // User has no password - show registration view
                loadRegistrationPortal(user);
                ApplicationLogger.getInstance().info("User redirected to registration screen. Enrollment will happen on next login.");
            }

        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            showAlert("Forgot Password Error", "An error occurred while processing your request: " + errorMsg);
            ApplicationLogger.getInstance().error("Error handling forgot password request", e);
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
     * Load dynamic UI for admin and student
     */
    public void loadAuthPortal(Role role) {
        try {
            // Determine view file based on role passed
            String viewFileUrl = (role == Role.ADMIN) ? baseViewFolderName + "/MainView.fxml"
                    : baseViewFolderName + "/StudentRootView.fxml";
            // Name of the role to display in the dashboard
            String roleName = (role == Role.ADMIN) ? "Admin" : "Student";

            // Load UI dynamically
            Parent authRoot = FXMLLoader.load(getClass().getResource(viewFileUrl));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(authRoot));
            stage.setTitle(roleName + " Portal");
            errorLabel.setText("");

            ApplicationLogger.getInstance().info("Loaded " + roleName + " portal successfully");
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            showAlert("Portal Loading Error", "Unable to open " + (role == Role.ADMIN ? "admin" : "student") + " portal: " + errorMsg);
            ApplicationLogger.getInstance().error("Error loading " + role + " portal", e);
        }
    }

    /**
     * Load facial enrollment UI
     */
    public void loadEnrollmentPortal() {
        try {
            Parent enrollmentRoot = FXMLLoader
                    .load(getClass().getResource(baseViewFolderName + "/EnrollmentView.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(enrollmentRoot));
            stage.setTitle("Face Enrollment");
            errorLabel.setText("");

            ApplicationLogger.getInstance().info("Loaded enrollment portal successfully");

            showAlert("Face Enrollment Required",
                    "Your facial data is not enrolled yet. Please complete the enrollment process to continue.");
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            showAlert("Enrollment Portal Error", "Unable to open enrollment portal: " + errorMsg);
            ApplicationLogger.getInstance().error("Error loading enrollment portal", e);
        }
    }

    public void loadForgetPasswordPortal(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ForgotPasswordView.fxml"));
            Parent resetRoot = loader.load();

            // Initialize the controller with user
            ForgotPasswordController resetController = loader.getController();
            resetController.initializeUser(user);

            // Navigate to password reset screen
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(resetRoot));
            stage.setTitle("Reset Password");

            ApplicationLogger.getInstance().info("Loaded forget password portal successfully for user: " + user.getUserName());
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            showAlert("Password Reset Error", "Unable to load password reset portal: " + errorMsg);
            ApplicationLogger.getInstance().error("Error loading forget password portal for user: " + user.getUserName(), e);
        }
    }

    public void loadRegistrationPortal(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterView.fxml"));
            Parent registerRoot = loader.load();

            // Initialize the controller with user
            RegisterController registerController = loader.getController();
            registerController.initializeUser(user);

            // Navigate to registration screen
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(registerRoot));
            stage.setTitle("Complete Registration");

            ApplicationLogger.getInstance().info("Loaded registration portal successfully for user: " + user.getUserName());
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            showAlert("Registration Error", "Unable to load registration portal: " + errorMsg);
            ApplicationLogger.getInstance().error("Error loading registration portal for user: " + user.getUserName(), e);
        }
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
