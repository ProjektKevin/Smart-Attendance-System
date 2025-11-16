package com.smartattendance.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import com.smartattendance.model.entity.Course;
import com.smartattendance.model.entity.Session;
import com.smartattendance.service.CourseService;
import com.smartattendance.service.SessionService;
import com.smartattendance.util.DialogUtil;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Session Creation Form dialog.
 * 
 * This controller handles the UI logic for creating new attendance sessions
 * through a modal form dialog. It validates user input, ensures business rule
 * compliance, and creates new session entities.
 * 
 * Key responsibilities:
 * - Form field initialisation and setup
 * - User input validation and error handling
 * - Business rule enforcement (time validation, date constraints)
 * - Session creation and dialog management
 * 
 * @author Lim Jia Hui
 * @version 19:16 16 Nov 2025
 */
public class SessionFormController {

    // ========== FXML COMPONENTS ==========

    /** Combo box for selecting the course associated with the session */
    @FXML
    private ComboBox<String> cmbCourse;

    /** Date picker for selecting the session date */
    @FXML
    private DatePicker datePicker;

    /**
     * Text field for entering session start time in HH:MM format (24 Hour Clock)
     */
    @FXML
    private TextField txtStart;

    /** Text field for entering session end time in HH:MM format (24 Hour Clock) */
    @FXML
    private TextField txtEnd;

    /** Text field for entering the physical location of the session */
    @FXML
    private TextField txtLocation;

    /** Text field for entering late threshold in minutes */
    @FXML
    private TextField txtLate;

    // ========== BUSINESS LOGIC DEPENDENCIES ==========

    /** The newly created session, populated after successful form submission */
    private Session newSession;

    /** Service for course-related operations and data retrieval */
    private final CourseService service = new CourseService();

    /** Utility for displaying standardized dialog messages */
    private final DialogUtil dialogUtil = new DialogUtil();

    // ========== INITIALIZATION METHODS ==========

    /**
     * Initializes the controller after FXML loading is complete.
     * 
     * Performs the following setup tasks:
     * - Populates the course combo box with available courses
     * - Sets up placeholder text and prompts for all form fields
     * - Configures default values and input constraints
     */
    @FXML
    public void initialize() {
        /**
         * Populates the course combo box with all available courses from the database.
         * Courses are displayed by their course codes in alphabetical order.
         */
        for (Course course : service.getCourses()) {
            cmbCourse.getItems().add(course.getCode());
        }

        // Set placeholder text for text fields
        setupPlaceholders();
    }

    /**
     * Sets up placeholder text and prompts for all form fields to guide user input.
     * This improves user experience by providing clear examples of expected input
     * formats.
     */
    private void setupPlaceholders() {
        // Set placeholder text for each text field
        txtStart.setPromptText("HH:MM (e.g., 09:00)");
        txtEnd.setPromptText("HH:MM (e.g., 13:00)");
        txtLocation.setPromptText("e.g., Room 101, Lab A");
        txtLate.setPromptText("Minutes (e.g., 15)");

        // Set prompt text for combo box
        cmbCourse.setPromptText("Select a course...");

        // Set prompt text for date picker
        datePicker.setPromptText("Select session date");
    }

    // ========== DATA ACCESS METHODS ==========

    /**
     * Returns the newly created session after successful form submission.
     * 
     * @return the created Session object, or null if no session was created
     */
    public Session getNewSession() {
        return newSession;
    }

    // ========== VALIDATION METHODS ==========

    /**
     * Validates whether the session end time is in the past relative to current
     * time.
     * Prevents creation of sessions that have already ended.
     *
     * @param date    the session date to validate
     * @param endTime the session end time to validate
     * @return true if the session end time is in the past, false otherwise
     * @throws IllegalArgumentException if date or endTime is null
     */
    private boolean isSessionEndTimeInPast(LocalDate date, LocalTime endTime) {
        LocalDateTime sessionEndDateTime = LocalDateTime.of(date, endTime);
        LocalDateTime currentDateTime = LocalDateTime.now();
        return sessionEndDateTime.isBefore(currentDateTime);
    }

    /**
     * Validates all form fields and returns true if all input is valid.
     * 
     * @return true if all form fields contain valid input, false otherwise
     */
    private boolean validateForm() {
        // Validate course selection
        if (cmbCourse.getValue() == null || cmbCourse.getValue().trim().isEmpty()) {
            dialogUtil.showError("Please select a course.");
            cmbCourse.requestFocus();
            return false;
        }

        // Validate date selection
        if (datePicker.getValue() == null) {
            dialogUtil.showError("Please select a session date.");
            datePicker.requestFocus();
            return false;
        }

        // Validate start time format and presence
        if (txtStart.getText() == null || txtStart.getText().trim().isEmpty()) {
            dialogUtil.showError("Please enter a start time in HH:MM format. (24 Hour Clock)");
            txtStart.requestFocus();
            return false;
        }

        // Validate end time format and presence
        if (txtEnd.getText() == null || txtEnd.getText().trim().isEmpty()) {
            dialogUtil.showError("Please enter an end time in HH:MM format. (24 Hour Clock)");
            txtEnd.requestFocus();
            return false;
        }

        // Validate location
        if (txtLocation.getText() == null || txtLocation.getText().trim().isEmpty()) {
            dialogUtil.showError("Please enter a session location.");
            txtLocation.requestFocus();
            return false;
        }

        // Validate late threshold
        if (txtLate.getText() == null || txtLate.getText().trim().isEmpty()) {
            dialogUtil.showError("Please enter a late threshold in minutes.");
            txtLate.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Parses time string into LocalTime object with proper error handling.
     * 
     * @param timeString the time string to parse (expected format: HH:MM)
     * @param fieldName  the name of the field for error messages
     * @return the parsed LocalTime object, or null if parsing failed
     */
    private LocalTime parseTime(String timeString, String fieldName) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(timeString.trim());
        } catch (DateTimeParseException e) {
            dialogUtil.showError(
                    String.format("Please enter a valid time for %s in HH:MM format (e.g., 09:00, 14:30).", fieldName));
            return null;
        }
    }

    /**
     * Parses late threshold string into integer with proper error handling.
     * 
     * @param lateString the late threshold string to parse
     * @return the parsed integer value, or null if parsing failed
     */
    private Integer parseLateThreshold(String lateString) {
        if (lateString == null || lateString.trim().isEmpty()) {
            return null;
        }

        try {
            int threshold = Integer.parseInt(lateString.trim());
            if (threshold < 0) {
                dialogUtil.showError(
                        "Late threshold cannot be negative. Please enter a positive number of minutes.");
                return null;
            }
            if (threshold > 1440) { // 24 hours in minutes
                dialogUtil.showError(
                        "Late threshold cannot exceed 24 hours (1440 minutes). Please enter a reasonable value.");
                return null;
            }
            return threshold;
        } catch (NumberFormatException e) {
            dialogUtil.showError(
                    "Please enter a valid number for late threshold (e.g., 15, 30).");
            return null;
        }
    }

    // ========== EVENT HANDLER METHODS ==========

    /**
     * Handles the cancel button action.
     * Closes the form dialog without creating a new session.
     */
    @FXML
    private void onCancel() {
        ((Stage) cmbCourse.getScene().getWindow()).close();
    }

    /**
     * Handles the create button action.
     * 
     * Performs the following steps:
     * - Validates all form field inputs
     * - Enforces business rules (time ordering, future sessions)
     * - Creates the session through the service layer
     * - Closes the dialog on success
     */
    @FXML
    private void onCreate() {
        try {
            // Basic form validation
            if (!validateForm()) {
                return;
            }

            String course = cmbCourse.getValue();
            LocalDate date = datePicker.getValue();

            LocalTime start = parseTime(txtStart.getText(), "start time");
            if (start == null) {
                txtStart.requestFocus();
                return;
            }

            LocalTime end = parseTime(txtEnd.getText(), "end time");
            if (end == null) {
                txtEnd.requestFocus();
                return;
            }

            String loc = txtLocation.getText();
            Integer late = parseLateThreshold(txtLate.getText());
            if (late == null)
                return;

            // Validate that the session end time is not in the past
            if (isSessionEndTimeInPast(date, end)) {
                dialogUtil.showError(
                        "Cannot create a session that has already ended. Please select a future date and time.");
                return;
            }

            // Validate that end time is after start time
            if (end.isBefore(start) || end.equals(start)) {
                dialogUtil.showError("End time must be after start time.");
                return;
            }

            // Create session through service layer
            newSession = new SessionService().createSession(course, date, start, end, loc, late);

            // Close dialog on success
            ((Stage) cmbCourse.getScene().getWindow()).close();

        } catch (Exception e) {
            dialogUtil.showError("Please check your fields: " + e.getMessage());
        }
    }
}