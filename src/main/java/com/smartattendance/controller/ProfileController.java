package com.smartattendance.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.AuthSession;
import com.smartattendance.model.Profile;
import com.smartattendance.model.User;
import com.smartattendance.service.ProfileService;
import com.smartattendance.service.AuthService;

/**
 * Controller for Profile view.
 * Handles displaying and editing user profile information.
 * Shows conditional UI: "Create Profile" button if no profile exists,
 * or "Edit Profile" button if profile exists.
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
    private TextField phoneField;

    @FXML
    private Label emailLabel;

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

    // ====== Dependencies ======

    private final ProfileService profileService = ApplicationContext.getProfileService();

    private final AuthSession session = ApplicationContext.getAuthSession();

    // ====== State ======

    private Profile currentProfile;
    private User currentUser;
    private boolean isEditMode = false;

    /**
     * Initialize the controller.
     * Called automatically by JavaFX after FXML loading.
     */
    @FXML
    public void initialize() {
        // Get current logged-in user
        currentUser = session.getCurrentUser();

        System.out.println("Logged In User: " + currentUser.getId());

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
            currentProfile = profileService.getUserProfile(currentUser.getId());

            if (currentProfile == null) {
                // No profile exists - show create profile UI
                showNoProfileState();
            } else {
                // Profile exists - show profile details UI
                showProfileState();
                displayProfileDetails();
            }
        } catch (Exception e) {
            showError("Failed to load profile: " + e.getMessage());
        }
    }

    /**
     * Display the "no profile" state.
     * Shows message and "Create Profile" button.
     */
    private void showNoProfileState() {
        noProfileContainer.setVisible(true);
        noProfileContainer.setManaged(true);
        profileContainer.setVisible(false);
        profileContainer.setManaged(false);
    }

    /**
     * Display the "profile exists" state.
     * Shows profile details and action buttons.
     */
    private void showProfileState() {
        noProfileContainer.setVisible(false);
        noProfileContainer.setManaged(false);
        profileContainer.setVisible(true);
        profileContainer.setManaged(true);
    }

    /**
     * Populate profile fields with data from currentProfile.
     */
    private void displayProfileDetails() {
        firstNameField.setText(currentProfile.getFirstName() != null
                ? currentProfile.getFirstName()
                : "");
        lastNameField.setText(currentProfile.getLastName() != null
                ? currentProfile.getLastName()
                : "");
        phoneField.setText(currentProfile.getPhoneNo() != null
                ? currentProfile.getPhoneNo()
                : "");
        emailLabel.setText(currentUser.getEmail());
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
            // Validate inputs
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String phone = phoneField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                showError("First name and last name are required");
                return;
            }

            // Update profile object
            currentProfile.setFirstName(firstName);
            currentProfile.setLastName(lastName);
            currentProfile.setPhoneNo(phone);

            // Save to database
            if (currentProfile.getProfileId() == null) {
                // New profile - insert
                profileService.createUserProfile(
                        firstName,
                        lastName,
                        phone,
                        currentUser.getId());
            } else {
                // Existing profile - update
                profileService.updateUserProfile(
                        firstName,
                        lastName,
                        phone,
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
        if (currentProfile != null && currentProfile.getProfileId() != null) {
            // Reload original profile data
            displayProfileDetails();
            exitEditMode();
            showStatus("");
        } else {
            // Creating new profile but cancelled - go back to no profile state
            currentProfile = null;
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
                currentProfile = null;
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
