package com.smartattendance.controller.admin;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.User;
import com.smartattendance.model.dto.student.StudentProfileDTO;
import com.smartattendance.model.dto.user.UserListDTO;
import com.smartattendance.service.AuthService;
import com.smartattendance.service.UserService;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

/**
 * A student list controller which connects the studentListView.fxml
 * Table inspried from the tanstack table
 * Link to tanstack table: https://tanstack.com/table/latest
 * 
 * @author Thiha Swan Htet
 **/
public class StudentListController {
    // Detail table
    @FXML
    private TableView<UserListDTO> table;

    @FXML
    private TableColumn<UserListDTO, Integer> actionsColumn;

    @FXML
    private TextField searchField;

    private UserService userService = ApplicationContext.getUserService();
    private AuthService authService = ApplicationContext.getAuthService();

    // Store all students for searching
    private List<UserListDTO> allStudents;

    // Initialize student data
    @FXML
    private void initialize() {
        // Setup actions column with buttons
        setupActionsColumn();
        loadStudents();
    }

    // Call DB for student data and set the table columns
    public void loadStudents() {
        // Fetch all students with STUDENT role as DTOs
        allStudents = userService.getStudentListDTOs();

        // Convert to ObservableList and populate table
        ObservableList<UserListDTO> observableStudents = FXCollections.observableArrayList(allStudents);
        table.setItems(observableStudents);
    }

    /*
     * The UI for the three dots under "More Actions"
     * Once clicked, three buttons: view profile, enroll course, and delete student
     * will pop up
     * Each button leads to its own dialog and controllers
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(
                new Callback<TableColumn<UserListDTO, Integer>, javafx.scene.control.TableCell<UserListDTO, Integer>>() {
                    @Override
                    public javafx.scene.control.TableCell<UserListDTO, Integer> call(
                            TableColumn<UserListDTO, Integer> param) {
                        return new javafx.scene.control.TableCell<UserListDTO, Integer>() {

                            // The three dots: more actions button menu
                            private final Button actionsBtn = new Button("...");
                            private final ContextMenu contextMenu = new ContextMenu();

                            {
                                // Create menu items
                                MenuItem viewProfileItem = new MenuItem("View Profile");
                                MenuItem enrollCourseItem = new MenuItem("Enroll Course");
                                MenuItem deleteItem = new MenuItem("Delete");

                                // Set action for View Profile
                                viewProfileItem.setOnAction(event -> {
                                    UserListDTO student = getTableView().getItems().get(getIndex());
                                    onViewProfile(student);
                                });

                                // Set action for Enroll Course
                                enrollCourseItem.setOnAction(event -> {
                                    UserListDTO student = getTableView().getItems().get(getIndex());
                                    onEnrollCourse(student);
                                });

                                // Set action for Delete
                                deleteItem.setOnAction(event -> {
                                    UserListDTO student = getTableView().getItems().get(getIndex());
                                    onDeleteStudent(student);
                                });

                                // Add items to context menu
                                contextMenu.getItems().addAll(viewProfileItem, enrollCourseItem, deleteItem);

                                // Button action shows context menu
                                actionsBtn.setOnAction(event -> {
                                    contextMenu.show(actionsBtn, javafx.geometry.Side.BOTTOM, 0, 0);
                                });

                                actionsBtn.setStyle("-fx-font-size: 11;");
                            }

                            @Override
                            protected void updateItem(Integer item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(actionsBtn);
                                }
                            }
                        };
                    }
                });
    }

    private void onViewProfile(UserListDTO studentDto) {
        // Fetch full student profile data for the dialog
        StudentProfileDTO profileDto = userService.getStudentProfileDTO(studentDto.getId());

        // Open student profile dialog with DTO
        StudentProfileDialog profileDialog = new StudentProfileDialog(profileDto);
        profileDialog.show();
    }

    private void onEnrollCourse(UserListDTO studentDto) {
        try {
            // Open Enroll Course dialog
            EnrollCourseDialog dialog = new EnrollCourseDialog(studentDto);
            dialog.show();

            // Check if user submitted the form
            if (!dialog.isSubmitted()) {
                return;
            }

            // Get courses to delete and add based on state comparison
            List<Integer> coursesToDelete = dialog.getCoursesToDelete();
            List<Integer> coursesToAdd = dialog.getCoursesToAdd();
            List<Integer> newCourseIds = dialog.getNewCourseIds();

            // Check if there are no courses selected
            if (newCourseIds.isEmpty()) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("No Courses Selected");
                alert.setHeaderText("Please select at least one course");
                alert.setContentText("You must select at least one course to enroll.");
                alert.showAndWait();
                return;
            }

            // Execute deletions
            for (Integer courseId : coursesToDelete) {
                userService.unenrollStudentFromCourse(studentDto.getId(), courseId);
            }

            // Execute additions
            for (Integer courseId : coursesToAdd) {
                userService.enrollStudentInCourse(studentDto.getId(), courseId);
            }

            // Show success message with summary
            StringBuilder summary = new StringBuilder();
            if (!coursesToAdd.isEmpty()) {
                summary.append("Added to ").append(coursesToAdd.size()).append(" course(s).\n");
            }
            if (!coursesToDelete.isEmpty()) {
                summary.append("Removed from ").append(coursesToDelete.size()).append(" course(s).\n");
            }
            if (coursesToAdd.isEmpty() && coursesToDelete.isEmpty()) {
                summary.append("No changes made.");
            }

            javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText("Enrollments Updated");
            successAlert.setContentText(summary.toString() + "\nStudent is now enrolled in " + newCourseIds.size()
                    + " course(s).");
            successAlert.showAndWait();

        } catch (Exception e) {
            javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Failed to manage enrollments");
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void onDeleteStudent(UserListDTO studentDto) {
        try {
            // Show confirmation dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Student");
            alert.setHeaderText("Are you sure you want to delete this student?");
            alert.setContentText("Email: " + studentDto.getEmail() + "\nThis action cannot be undone.");

            if (alert.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
                // Delete User using the student ID from the DTO
                userService.deleteUser(studentDto.getId());

                // Reload to the students table
                Platform.runLater(this::loadStudents);

                // Show success message
                javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Student Deleted");
                successAlert.setContentText("Student has been successfully deleted.");
                successAlert.showAndWait();
            }
        } catch (Exception e) {
            javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Failed to delete student");
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();
        }
    }

    @FXML
    private void onAddStudent() {
        try {
            // Open Add Student dialog
            AddStudentDialog dialog = new AddStudentDialog();
            dialog.show();

            // Check if user submitted the form
            if (!dialog.isSubmitted()) {
                return;
            }

            String email = dialog.getEmail();
            String role = dialog.getRole();

            // Check if user already exists
            User user = authService.getUserByEmail(email);

            if (user != null) {
                // User already exists - show error message
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("User Already Exists");
                alert.setHeaderText("Cannot Add Student");
                alert.setContentText("A user with email '" + email + "' already exists.");
                alert.showAndWait();
                return;
            }

            // Invite new user
            boolean isUserAdded = authService.inviteUser(email, role);

            // Always reload table to ensure we have latest data
            // (AuthRepository might swallow exceptions, so check actual database state)
            loadStudents();

            if (isUserAdded) {
                // Success message
                javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Student Added");
                successAlert.setContentText("User Added: " + email);
                successAlert.showAndWait();
            } else {
                // Failure message - invitation returned false
                javafx.scene.control.Alert failAlert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING);
                failAlert.setTitle("Failed");
                failAlert.setHeaderText("Could Not Add Student");
                failAlert.setContentText("Failed to create account for: " + email);
                failAlert.showAndWait();
            }

        } catch (Exception e) {
            javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Failed to add student");
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();
        }
    }

    /**
     * Search students by email (case-insensitive, partial match)
     * Filters the table to show only students matching the search query
     */
    @FXML
    private void onSearchStudent() {
        String searchText = searchField.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            // If search is empty, show all students
            ObservableList<UserListDTO> observableStudents = FXCollections.observableArrayList(allStudents);
            table.setItems(observableStudents);
            return;
        }

        // Filter students by email containing search text (case-insensitive)
        List<UserListDTO> filteredList = new java.util.ArrayList<>();
        for (UserListDTO student : allStudents) {
            if (student.getEmail().toLowerCase().contains(searchText)) {
                filteredList.add(student);
            }
        }

        ObservableList<UserListDTO> observableStudents = FXCollections.observableArrayList(filteredList);
        table.setItems(observableStudents);
    }

    /**
     * Clear the search and show all students
     */
    @FXML
    private void onClearSearch() {
        searchField.clear();
        ObservableList<UserListDTO> observableStudents = FXCollections.observableArrayList(allStudents);
        table.setItems(observableStudents);
    }

}
