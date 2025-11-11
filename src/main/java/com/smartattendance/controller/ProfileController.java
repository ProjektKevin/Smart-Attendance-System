package com.smartattendance.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.application.Platform;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.dto.user.UserProfileDTO;
import com.smartattendance.model.entity.AuthSession;
import com.smartattendance.model.entity.User;
import com.smartattendance.model.entity.Course;
import com.smartattendance.model.enums.Role;
import com.smartattendance.service.UserService;
import com.smartattendance.service.ProfileService;
import com.smartattendance.service.CourseService;
import com.smartattendance.util.validation.ProfileValidator;
import com.smartattendance.util.validation.ValidationResult;

import java.util.List;
import java.util.Map;

/**
 * Controller for Profile view.
 * Handles displaying and editing user profile information.
 * Shows conditional UI: "Create Profile" button if no profile exists,
 * or "Edit Profile" button if profile exists.
 * 
 * @author Thiha Swan Htet
 */
public class ProfileController {

    // ====== FXML Components ======

    // No Profile State Container
    @FXML
    private VBox noProfileContainer;

    // Profile Exists State Container
    @FXML
    private VBox profileContainer;

    // Profile Information Fields
    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    // Error Labels (displayed under each field)
    @FXML
    private Label firstNameError;

    @FXML
    private Label lastNameError;

    @FXML
    private Label phoneError;

    // View Mode Buttons
    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    // Edit Mode Buttons
    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    // Status/Error Messages
    @FXML
    private Label statusLabel;

    @FXML
    private Label errorLabel;

    // Courses Section Components
    @FXML
    private VBox coursesContainer;

    @FXML
    private Label courseCountLabel;

    @FXML
    private FlowPane coursesFlowPane;

    @FXML
    private Label noCourseLabel;

    // ====== Dependencies ======

    private final UserService userService = ApplicationContext.getUserService();
    private final ProfileService profileService = ApplicationContext.getProfileService();
    private final CourseService courseService = ApplicationContext.getCourseService();
    private final AuthSession session = ApplicationContext.getAuthSession();

    // ====== State ======
    private User currentUser;
    private UserProfileDTO userProfile;
    private boolean isEditMode = false;

    /**
     * Initialize the controller.
     * Called automatically after UI loads.
     */
    @FXML
    public void initialize() {
        // Get current logged-in user
        currentUser = session.getCurrentUser();

        if (currentUser == null) {
            showError("No user logged in");
            return;
        }

        // Load profile for current user
        loadProfile();

        // Clear messages
        statusLabel.setText("");
        errorLabel.setText("");
    }

    /**
     * Load user's profile from database.
     * Shows "Create Profile" screen if profile doesn't exist,
     * or shows profile details if it does exist.
     */
    private void loadProfile() {
        try {
            // Show profile details UI
            showProfileState();
            displayProfileDetails();

        } catch (Exception e) {
            // chore(), Harry: Change back to logger with a different log level
            System.out.println("Error loading profile: " + e.getMessage());
            showError("Failed to load profile: ");
        }
    }

    /**
     * Display the "profile exists" state.
     * Shows profile details and action buttons.
     */
    private void showProfileState() {
        profileContainer.setVisible(true);
        profileContainer.setManaged(true);
    }

    /**
     * Populate profile fields with data from userProfile.
     */
    private void displayProfileDetails() {
        try {
            userProfile = userService.getUserProfileDTO(currentUser.getId());
            firstNameField.setText(userProfile.getFirstName() != null
                    ? userProfile.getFirstName()
                    : "");
            lastNameField.setText(userProfile.getLastName() != null
                    ? userProfile.getLastName()
                    : "");
            phoneField.setText(userProfile.getPhoneNo() != null
                    ? userProfile.getPhoneNo()
                    : "");
            emailField.setText(userProfile.getEmail() != null
                    ? userProfile.getEmail()
                    : "");

            if (currentUser.getRole() == Role.STUDENT) {
                loadEnrolledCourses();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Load and display enrolled courses if the user is a STUDENT.
     * Shows courses in a responsive FlowPane with styled labels.
     */
    private void loadEnrolledCourses() {
        try {
            // Check if current user is a STUDENT
            if (currentUser.getRole() != Role.STUDENT) {
                // Hide courses section for non-students (ADMIN, INSTRUCTOR, etc.)
                coursesContainer.setVisible(false);
                coursesContainer.setManaged(false);
                return;
            }

            // Show courses section for students
            coursesContainer.setVisible(true);
            coursesContainer.setManaged(true);

            // Load all courses and filter by enrollment
            List<Course> enrolledCourses = courseService.getCoursesByStudentId(currentUser.getId());

            // Update course count
            courseCountLabel.setText("Total: " + enrolledCourses.size() + " course(s)");

            // Clear previous courses from FlowPane
            coursesFlowPane.getChildren().clear();

            // Display courses or "no courses" message
            if (!enrolledCourses.isEmpty()) {
                noCourseLabel.setVisible(false);
                noCourseLabel.setManaged(false);

                for (Course course : enrolledCourses) {
                    // Create a styled label for each course
                    Label courseLabel = new Label(course.getCode() + " - " + course.getName());
                    courseLabel.setStyle(
                            "-fx-background-color: #e3f2fd; " +
                                    "-fx-border-color: #2196f3; " +
                                    "-fx-border-width: 1; " +
                                    "-fx-padding: 6 12; " +
                                    "-fx-border-radius: 4; " +
                                    "-fx-text-fill: #1976d2; " +
                                    "-fx-font-size: 11; " +
                                    "-fx-font-weight: 500;");
                    coursesFlowPane.getChildren().add(courseLabel);
                }
            } else {
                noCourseLabel.setVisible(true);
                noCourseLabel.setManaged(true);
            }
        } catch (Exception e) {
            System.out.println("Error loading enrolled courses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle "Create Profile" button click.
     * Switches to edit mode and enables form fields.
     */
    @FXML
    private void onCreateProfile() {
        try {
            // Switch to profile view in edit mode
            showProfileState();
            clearFields();
            enterEditMode();
            showStatus("Enter your profile details");
        } catch (Exception e) {
            showError("Failed to create profile: " + e.getMessage());
        }
    }

    /**
     * Handle "Edit Profile" button click.
     * Enables editing of profile fields.
     */
    @FXML
    private void onEditProfile() {
        enterEditMode();
        showStatus("You are now editing your profile");
    }

    /**
     * Enable edit mode.
     * Makes text fields editable and shows save/cancel buttons.
     */
    private void enterEditMode() {
        isEditMode = true;

        // Clear all error messages
        clearFieldErrors();

        // Enable field editing
        firstNameField.setEditable(true);
        lastNameField.setEditable(true);
        phoneField.setEditable(true);

        // Toggle button visibility
        editButton.setVisible(false);
        editButton.setManaged(false);
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);

        saveButton.setVisible(true);
        saveButton.setManaged(true);
        cancelButton.setVisible(true);
        cancelButton.setManaged(true);
    }

    /**
     * Handle "Save Changes" button click.
     * Validates and saves profile to database.
     */
    @FXML
    private void onSaveProfile() {
        try {
            // Get input values
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String phone = phoneField.getText();

            // Validate all fields using ProfileValidator
            // This returns a ValidationResult with field errors
            ValidationResult validationResult = ProfileValidator.validateProfile(
                    firstName, lastName, phone);

            // If validation failed, display errors under each field
            if (!validationResult.isValid()) {
                displayFieldErrors(validationResult);
                return;
            }

            // All validations passed - clear field errors
            clearFieldErrors();

            // Save to database
            if (userProfile == null) {
                // New profile - insert
                // chore(), Harry: Add logger and info dialog if unsuccessful
                profileService.createUserProfile(
                        firstName.trim(),
                        lastName.trim(),
                        phone.trim(),
                        currentUser.getId());
            } else {
                // Existing profile - update
                profileService.updateUserProfile(
                        firstName.trim(),
                        lastName.trim(),
                        phone.trim(),
                        currentUser.getId());
            }

            // Reload profile to get updated data
            loadProfile();
            exitEditMode();
            showStatus("Profile saved successfully");
        } catch (Exception e) {
            showError("Failed to save profile: " + e.getMessage());
        }
    }

    /**
     * Handle "Cancel" button click.
     * Discard changes and exit edit mode.
     */
    @FXML
    private void onCancelEdit() {
        if (userProfile != null) {
            // Reload original profile data
            displayProfileDetails();
            exitEditMode();
            showStatus("");
        } else {
            // Creating new profile but cancelled - go back to no profile state
            userProfile = null;
            loadProfile();
        }
    }

    /**
     * Exit edit mode.
     * Makes text fields read-only and shows edit/delete buttons.
     */
    private void exitEditMode() {
        isEditMode = false;

        // Disable field editing
        firstNameField.setEditable(false);
        lastNameField.setEditable(false);
        phoneField.setEditable(false);

        // Clear error messages
        clearFieldErrors();

        // Toggle button visibility
        editButton.setVisible(true);
        editButton.setManaged(true);
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);

        saveButton.setVisible(false);
        saveButton.setManaged(false);
        cancelButton.setVisible(false);
        cancelButton.setManaged(false);
    }

    /**
     * Handle "Delete Profile" button click.
     * Deletes the profile after confirmation.
     */
    @FXML
    private void onDeleteProfile() {
        try {
            // Show confirmation dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Profile");
            alert.setHeaderText("Are you sure you want to delete your profile?");
            alert.setContentText("This action cannot be undone.");

            if (alert.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
                // Delete profile
                profileService.deleteUserProfile(currentUser.getId());
                userProfile = null;
                showStatus("Profile deleted successfully");

                // Reload to show "Create Profile" screen
                Platform.runLater(this::loadProfile);
            }
        } catch (Exception e) {
            showError("Failed to delete profile: " + e.getMessage());
        }
    }

    /**
     * Clear all input fields.
     */
    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        clearFieldErrors();
    }

    /**
     * Display field-specific validation errors under each input field.
     * Maps ValidationResult errors to their corresponding error labels.
     * Works like Zod in React - each field shows its own error.
     *
     * @param validationResult the ValidationResult from ProfileValidator
     */
    private void displayFieldErrors(ValidationResult validationResult) {
        // Clear all errors first
        clearFieldErrors();

        // Get the error map (field name -> error message)
        Map<String, String> fieldErrors = validationResult.getAllFieldErrors();

        // Display error under firstName field if it exists
        if (fieldErrors.containsKey("firstName")) {
            firstNameError.setText(fieldErrors.get("firstName"));
        }

        // Display error under lastName field if it exists
        if (fieldErrors.containsKey("lastName")) {
            lastNameError.setText(fieldErrors.get("lastName"));
        }

        // Display error under phone field if it exists
        if (fieldErrors.containsKey("phone")) {
            phoneError.setText(fieldErrors.get("phone"));
        }
    }

    /**
     * Clear all field-specific error messages.
     * Hides all error labels under input fields.
     */
    private void clearFieldErrors() {
        firstNameError.setText("");
        lastNameError.setText("");
        phoneError.setText("");
    }

    /**
     * Display a status message (green color).
     */
    private void showStatus(String message) {
        statusLabel.setText(message);
        errorLabel.setText("");
    }

    /**
     * Display an error message (red color).
     */
    private void showError(String message) {
        errorLabel.setText(message);
        statusLabel.setText("");
    }
}
