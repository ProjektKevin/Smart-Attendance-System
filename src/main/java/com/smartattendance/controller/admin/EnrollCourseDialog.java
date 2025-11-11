package com.smartattendance.controller.admin;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.Course;
import com.smartattendance.model.dto.user.UserListDTO;
import com.smartattendance.service.CourseService;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Modal dialog for managing student course enrollments
 * Supports viewing, adding, editing, and deleting course enrollments
 * Displays up to 4 course slots with smart state tracking
 * 
 * @author Thiha Swan Htet
 */
public class EnrollCourseDialog {
    private final Stage stage;
    private final UserListDTO student;
    private final CourseService courseService = ApplicationContext.getCourseService();

    private boolean submitted = false;

    // State tracking
    private List<Integer> originalCourseIds = new ArrayList<>(); // Current enrollments
    private List<Integer> newCourseIds = new ArrayList<>(); // After user edits

    // UI Components
    private List<ComboBox<String>> courseComboBoxes = new ArrayList<>();
    private List<Label> courseErrorLabels = new ArrayList<>();
    private List<Button> deleteCourseButtons = new ArrayList<>();
    private List<Boolean> isExistingCourse = new ArrayList<>(); // Track if slot was originally filled
    private VBox formContainer;
    private Button addCourseBtn;
    private Label statusLabel;

    private static final int MAX_COURSES = 4;

    public EnrollCourseDialog(UserListDTO student) {
        this.student = student;
        this.stage = new Stage();
        loadExistingEnrollments();
        initializeDialog();
    }

    /**
     * Load existing course enrollments for the student from database
     */
    private void loadExistingEnrollments() {
        try {
            // Get student's enrolled courses directly from database
            List<Course> enrolledCourses = courseService.getCoursesByStudentId(student.getId());

            for (Course course : enrolledCourses) {
                originalCourseIds.add(course.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeDialog() {
        // Set up stage properties
        stage.setTitle("Manage Student Enrollments");
        stage.setWidth(550);
        stage.setHeight(500);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        // Create root layout
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
        root.setFillWidth(true);

        // Create title
        Label titleLabel = new Label("Manage Courses for " + student.getEmail());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Create info label showing current enrollment count
        Label infoLabel = new Label("Currently enrolled in " + originalCourseIds.size() + " course(s). Max: 4");
        infoLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        // Create form container with scrolling capability
        formContainer = new VBox(15);
        formContainer.setPadding(new Insets(10, 0, 10, 0));

        // Create "Add Course" button first (before adding fields)
        addCourseBtn = new Button("+ Add Course");
        addCourseBtn.setStyle("-fx-padding: 6 16; -fx-font-size: 11;");
        addCourseBtn.setOnAction(event -> handleAddCourseField());

        // Fill form with existing courses first, then empty slots
        fillFormWithExistingAndEmpty();

        HBox addCourseBox = new HBox();
        addCourseBox.getChildren().add(addCourseBtn);

        // Create status label
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #4caf50;");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        // Create button panel
        HBox buttonPanel = createButtonPanel();

        // Create a scrollable container for the form
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(formContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(titleLabel, infoLabel, scrollPane, addCourseBox, statusLabel, buttonPanel);

        // Create scene and set it
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    /**
     * Fill the form with existing enrollments first, then empty slots up to
     * MAX_COURSES
     */
    private void fillFormWithExistingAndEmpty() {
        int totalSlots = Math.max(originalCourseIds.size(), 1); // At least 1 slot
        totalSlots = Math.min(totalSlots, MAX_COURSES); // Max 4 slots

        // Create slots for all existing courses
        for (Integer courseId : originalCourseIds) {
            addCourseFieldWithExisting(courseId, true);
        }

        // Fill remaining slots with empty ones (up to 4 total)
        while (courseComboBoxes.size() < MAX_COURSES && courseComboBoxes.size() < totalSlots) {
            addCourseFieldEmpty();
        }
    }

    /**
     * Add a course field with an existing enrollment
     */
    private void addCourseFieldWithExisting(Integer courseId, boolean isExisting) {
        int fieldIndex = courseComboBoxes.size();
        Course existingCourse = courseService.getCourse(courseId);

        // Create horizontal box for this course field
        HBox courseFieldBox = new HBox(10);
        courseFieldBox.setStyle(
                "-fx-border-color: #f0f0f0; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: #f9f9f9;");

        // Label
        Label courseLabel = new Label("Course " + (fieldIndex + 1) + ":");
        courseLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 80;");

        // ComboBox for course selection
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select a course");
        courseComboBox.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        courseComboBox.setPrefWidth(250);

        // Populate with courses
        populateCourses(courseComboBox);

        // Select the existing course
        if (existingCourse != null) {
            String displayText = existingCourse.getCode() + " - " + existingCourse.getName();
            courseComboBox.setValue(displayText);
        }

        // Error label
        Label courseError = new Label("");
        courseError.setStyle("-fx-font-size: 10; -fx-text-fill: #d32f2f;");
        courseError.setVisible(false);
        courseError.setManaged(false);
        courseError.setWrapText(true);

        // Delete button (always visible for existing courses)
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 10;");
        deleteBtn.setOnAction(event -> handleDeleteCourseField(fieldIndex));

        // VBox to hold combobox and error message
        VBox fieldContent = new VBox(3);
        HBox comboAndDeleteBox = new HBox(5);
        comboAndDeleteBox.getChildren().addAll(courseComboBox, deleteBtn);
        HBox.setHgrow(courseComboBox, Priority.ALWAYS);
        fieldContent.getChildren().addAll(comboAndDeleteBox, courseError);

        courseFieldBox.getChildren().addAll(courseLabel, fieldContent);
        HBox.setHgrow(fieldContent, Priority.ALWAYS);

        // Add to lists and form container
        courseComboBoxes.add(courseComboBox);
        courseErrorLabels.add(courseError);
        deleteCourseButtons.add(deleteBtn);
        isExistingCourse.add(isExisting);
        formContainer.getChildren().add(courseFieldBox);

        // Update button state
        updateAddCourseButtonState();
    }

    /**
     * Add an empty course field
     */
    private void addCourseFieldEmpty() {
        int fieldIndex = courseComboBoxes.size();

        // Create horizontal box for this course field
        HBox courseFieldBox = new HBox(10);
        courseFieldBox.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 1; -fx-padding: 10;");

        // Label
        Label courseLabel = new Label("Course " + (fieldIndex + 1) + ":");
        courseLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 80;");

        // ComboBox for course selection
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select a course");
        courseComboBox.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        courseComboBox.setPrefWidth(250);

        // Populate with courses
        populateCourses(courseComboBox);

        // Error label
        Label courseError = new Label("");
        courseError.setStyle("-fx-font-size: 10; -fx-text-fill: #d32f2f;");
        courseError.setVisible(false);
        courseError.setManaged(false);
        courseError.setWrapText(true);

        // Delete button (hidden for empty slots)
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 10;");
        deleteBtn.setVisible(false);
        deleteBtn.setManaged(false);
        deleteBtn.setOnAction(event -> handleDeleteCourseField(fieldIndex));

        // VBox to hold combobox and error message
        VBox fieldContent = new VBox(3);
        HBox comboAndDeleteBox = new HBox(5);
        comboAndDeleteBox.getChildren().addAll(courseComboBox, deleteBtn);
        HBox.setHgrow(courseComboBox, Priority.ALWAYS);
        fieldContent.getChildren().addAll(comboAndDeleteBox, courseError);

        courseFieldBox.getChildren().addAll(courseLabel, fieldContent);
        HBox.setHgrow(fieldContent, Priority.ALWAYS);

        // Add to lists and form container
        courseComboBoxes.add(courseComboBox);
        courseErrorLabels.add(courseError);
        deleteCourseButtons.add(deleteBtn);
        isExistingCourse.add(false);
        formContainer.getChildren().add(courseFieldBox);

        // Update button state
        updateAddCourseButtonState();
    }

    /**
     * Add a new empty course field
     */
    private void handleAddCourseField() {
        if (courseComboBoxes.size() >= MAX_COURSES) {
            return;
        }
        addCourseFieldEmpty();
    }

    /**
     * Remove a course field by index
     */
    private void handleDeleteCourseField(int index) {
        if (courseComboBoxes.size() <= 1) {
            setInfoDialog(javafx.scene.control.Alert.AlertType.WARNING, "Warning", "Course Deletion",
                    "Student Must Be Enrolled to At Least One Course");
            return;
        }

        // Remove from lists
        courseComboBoxes.remove(index);
        courseErrorLabels.remove(index);
        deleteCourseButtons.remove(index);
        isExistingCourse.remove(index);

        // Remove from form container
        formContainer.getChildren().remove(index);

        // Update button state
        updateAddCourseButtonState();
    }

    /**
     * Update the state of the "Add Course" button
     */
    private void updateAddCourseButtonState() {
        addCourseBtn.setDisable(courseComboBoxes.size() >= MAX_COURSES);
    }

    /**
     * Populate course combobox with available courses
     */
    private void populateCourses(ComboBox<String> comboBox) {
        try {
            List<Course> courses = courseService.getCourses();
            for (Course course : courses) {
                // Display as "Code - Name" format
                String displayText = course.getCode() + " - " + course.getName();
                comboBox.getItems().add(displayText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Validate form and handle enrollment state changes
     */
    private void handleEnrollCourses() {
        clearFieldErrors();
        newCourseIds.clear();

        boolean isValid = true;
        List<Course> allCourses = courseService.getCourses();

        // Validate and collect all selected courses
        for (int i = 0; i < courseComboBoxes.size(); i++) {
            ComboBox<String> comboBox = courseComboBoxes.get(i);
            String selectedValue = comboBox.getValue();
            Label errorLabel = courseErrorLabels.get(i);

            // Empty slots are allowed
            if (selectedValue == null || selectedValue.isEmpty()) {
                continue;
            }

            // Find the selected course by code
            String courseCode = selectedValue.split(" - ")[0].trim();
            Course selectedCourse = allCourses.stream()
                    .filter(c -> c.getCode().equals(courseCode))
                    .findFirst()
                    .orElse(null);

            if (selectedCourse == null) {
                errorLabel.setText("Invalid course selection");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                isValid = false;
                continue;
            }

            // Check for duplicates
            if (newCourseIds.contains(selectedCourse.getId())) {
                errorLabel.setText("Duplicate course selection");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                isValid = false;
                continue;
            }

            newCourseIds.add(selectedCourse.getId());
        }

        if (!isValid) {
            return;
        }

        // Mark as submitted
        submitted = true;
        stage.close();
    }

    /**
     * Clear all error messages
     */
    private void clearFieldErrors() {
        for (Label errorLabel : courseErrorLabels) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * Create button panel with Submit and Cancel buttons
     */
    private HBox createButtonPanel() {
        HBox panel = new HBox(10);
        panel.setStyle("-fx-alignment: center-right;");

        Button submitBtn = new Button("Save Enrollments");
        submitBtn.setStyle("-fx-padding: 8 24; -fx-font-size: 12;");
        submitBtn.setOnAction(event -> handleEnrollCourses());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-padding: 8 24; -fx-font-size: 12;");
        cancelBtn.setOnAction(event -> stage.close());

        panel.getChildren().addAll(submitBtn, cancelBtn);
        return panel;
    }

    /**
     * Show the dialog modally and wait for result
     */
    public void show() {
        stage.showAndWait();
    }

    /**
     * Check if the dialog was submitted successfully
     */
    public boolean isSubmitted() {
        return submitted;
    }

    /**
     * Show an alert dialog to the user
     */
    /**
     * Show an alert dialog to the user
     */
    public void setInfoDialog(javafx.scene.control.Alert.AlertType alertType, String title, String headerText,
            String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Get the original (current) course IDs
     */
    public List<Integer> getOriginalCourseIds() {
        return new ArrayList<>(originalCourseIds);
    }

    /**
     * Get the new course IDs after user edits
     */
    public List<Integer> getNewCourseIds() {
        return new ArrayList<>(newCourseIds);
    }

    /**
     * Get courses to be deleted (in original but not in new)
     */
    public List<Integer> getCoursesToDelete() {
        List<Integer> toDelete = new ArrayList<>(originalCourseIds);
        toDelete.removeAll(newCourseIds);
        return toDelete;
    }

    /**
     * Get courses to be added (in new but not in original)
     */
    public List<Integer> getCoursesToAdd() {
        List<Integer> toAdd = new ArrayList<>(newCourseIds);
        toAdd.removeAll(originalCourseIds);
        return toAdd;
    }

    /**
     * Get the student being managed
     */
    public UserListDTO getStudent() {
        return student;
    }
}
