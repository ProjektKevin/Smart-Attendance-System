package com.smartattendance.controller.admin;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.dto.user.UserListDTO;
import com.smartattendance.model.dto.user.UserProfileDTO;
import com.smartattendance.service.UserService;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class StudentListController {
    // Detail table
    @FXML
    private TableView<UserListDTO> table;

    @FXML
    private TableColumn<UserListDTO, Integer> actionsColumn;

    private UserService userService = ApplicationContext.getUserService();

    // Initialize student data
    @FXML
    private void initialize() {
        // Setup actions column with buttons
        setupActionsColumn();
        loadStudents();
    }

    public void loadStudents() {
        // Fetch all students with STUDENT role as DTOs
        List<UserListDTO> students = userService.getStudentListDTOs();

        // Convert to ObservableList and populate table
        ObservableList<UserListDTO> observableStudents = FXCollections.observableArrayList(students);
        table.setItems(observableStudents);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(
                new Callback<TableColumn<UserListDTO, Integer>, javafx.scene.control.TableCell<UserListDTO, Integer>>() {
                    @Override
                    public javafx.scene.control.TableCell<UserListDTO, Integer> call(
                            TableColumn<UserListDTO, Integer> param) {
                        return new javafx.scene.control.TableCell<UserListDTO, Integer>() {
                            private final Button actionsBtn = new Button("...");
                            private final ContextMenu contextMenu = new ContextMenu();

                            {
                                // Create menu items
                                MenuItem viewProfileItem = new MenuItem("View Profile");
                                MenuItem deleteItem = new MenuItem("Delete");

                                // Set action for View Profile
                                viewProfileItem.setOnAction(event -> {
                                    UserListDTO student = getTableView().getItems().get(getIndex());
                                    onViewProfile(student);
                                });

                                // Set action for Delete
                                deleteItem.setOnAction(event -> {
                                    UserListDTO student = getTableView().getItems().get(getIndex());
                                    onDeleteStudent(student);
                                });

                                // Add items to context menu
                                contextMenu.getItems().addAll(viewProfileItem, deleteItem);

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
        UserProfileDTO profileDto = userService.getUserProfileDTO(studentDto.getId());

        // Open student profile dialog with DTO
        StudentProfileDialog profileDialog = new StudentProfileDialog(profileDto);
        profileDialog.show();
    }

    private void onDeleteStudent(UserListDTO studentDto) {
        // chore(), Harry: Add delete method here
        System.out.println("Delete student: " + studentDto.getEmail());
    }

    // chore(), Harry: Finish this up (user registration)
    private void onAddStudent() {
        try {
            // String email = emailField.getText();

            // ValidationResult validationResult = 
        } catch (Exception e) {
        }
    }
}
