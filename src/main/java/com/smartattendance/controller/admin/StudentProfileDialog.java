package com.smartattendance.controller.admin;

import com.smartattendance.model.dto.user.UserProfileDTO;
import com.smartattendance.model.enums.Status;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Modal dialog for displaying student profile information
 * Uses UserProfileDTO for displaying student data
 */
public class StudentProfileDialog {
    private final Stage stage;
    private final UserProfileDTO student;

    public StudentProfileDialog(UserProfileDTO student) {
        this.student = student;
        this.stage = new Stage();
        initializeDialog();
    }

    private void initializeDialog() {
        // Set up stage properties - responsive sizing
        stage.setTitle("Student Profile");
        stage.setWidth(550);
        stage.setHeight(500);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(true); // Allow user to resize
        stage.setMinWidth(400); // Minimum size
        stage.setMinHeight(350);

        // Create root layout
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
        root.setFillWidth(true);

        // Create title
        Label titleLabel = new Label("Student Profile");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        // Create form grid with scroll pane
        ScrollPane scrollPane = new ScrollPane();
        GridPane formGrid = createProfileGrid();
        scrollPane.setContent(formGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        // Make scroll pane grow to fill available space
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Close button
        javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("Close");
        closeBtn.setStyle("-fx-padding: 8; -fx-font-size: 12;");
        closeBtn.setOnAction(event -> stage.close());

        root.getChildren().addAll(titleLabel, scrollPane, closeBtn);

        // Create scene and set it
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    private GridPane createProfileGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        // Student ID
        Label idLabel = new Label("Student ID:");
        idLabel.setStyle("-fx-font-weight: bold;");
        Label idValue = new Label(student.getId() != null ? student.getId().toString() : "N/A");

        // First Name
        Label firstNameLabel = new Label("First Name:");
        firstNameLabel.setStyle("-fx-font-weight: bold;");
        Label firstNameValue = new Label(student.getFirstName() != null ? student.getFirstName() : "N/A");

        // Last Name
        Label lastNameLabel = new Label("Last Name:");
        lastNameLabel.setStyle("-fx-font-weight: bold;");
        Label lastNameValue = new Label(student.getLastName() != null ? student.getLastName() : "N/A");

        // Email
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-weight: bold;");
        Label emailValue = new Label(student.getEmail() != null ? student.getEmail() : "N/A");

        // Phone Number
        Label phoneLabel = new Label("Phone Number:");
        phoneLabel.setStyle("-fx-font-weight: bold;");
        Label phoneValue = new Label(student.getPhoneNo() != null ? student.getPhoneNo() : "N/A");

        // Email Verified
        Label verifiedLabel = new Label("Email Verified:");
        verifiedLabel.setStyle("-fx-font-weight: bold;");
        Label verifiedValue = new Label(student.isEmailVerified() ? Status.VERIFIED.name() : Status.PENDING.name());

        // Role
        Label roleLabel = new Label("Role:");
        roleLabel.setStyle("-fx-font-weight: bold;");
        Label roleValue = new Label(student.getRole() != null ? student.getRole().name() : "N/A");

        // Add to grid
        int row = 0;
        grid.add(idLabel, 0, row);
        grid.add(idValue, 1, row++);

        grid.add(firstNameLabel, 0, row);
        grid.add(firstNameValue, 1, row++);

        grid.add(lastNameLabel, 0, row);
        grid.add(lastNameValue, 1, row++);

        grid.add(emailLabel, 0, row);
        grid.add(emailValue, 1, row++);

        grid.add(phoneLabel, 0, row);
        grid.add(phoneValue, 1, row++);

        grid.add(verifiedLabel, 0, row);
        grid.add(verifiedValue, 1, row++);

        grid.add(roleLabel, 0, row);
        grid.add(roleValue, 1, row);

        // Set column constraints
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints(120);
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setFillWidth(true);
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        return grid;
    }

    /**
     * Show the dialog modally
     */
    public void show() {
        stage.showAndWait();
    }
}
