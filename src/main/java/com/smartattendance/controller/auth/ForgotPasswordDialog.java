package com.smartattendance.controller.auth;

import java.util.Map;

import com.smartattendance.util.validation.AuthValidator;
import com.smartattendance.util.validation.ValidationResult;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Modal dialog for forgot password
 * Accepts email as input
 * 
 * @author Thiha Swan Htet
 */
public class ForgotPasswordDialog {
    private final Stage stage;
    private boolean submitted = false;
    private String resultEmail;

    // UI Components
    private TextField emailField;
    private Label emailError;
    private Label statusLabel;

    public ForgotPasswordDialog() {
        this.stage = new Stage();
        initializeDialog();
    }

    private void initializeDialog() {
        // Set up stage properties
        stage.setTitle("Forgot Password");
        stage.setWidth(450);
        stage.setHeight(350);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        // Create root layout
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
        root.setFillWidth(true);

        // Create title
        Label titleLabel = new Label("Forgot Password");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Create form grid
        GridPane formGrid = createFormGrid();

        // Create status label
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #4caf50;");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        // Create button panel
        HBox buttonPanel = createButtonPanel();

        root.getChildren().addAll(titleLabel, formGrid, statusLabel, buttonPanel);

        // Create scene and set it
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        // Email Field
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-weight: bold;");
        emailField = new TextField();
        emailField.setPromptText("student@example.com");
        emailField.setStyle("-fx-font-size: 12; -fx-padding: 8;");

        emailError = new Label("");
        emailError.setStyle("-fx-font-size: 11; -fx-text-fill: #d32f2f;");
        emailError.setVisible(false);
        emailError.setManaged(false);
        emailError.setWrapText(true);

        VBox emailBox = new VBox(4);
        emailBox.getChildren().addAll(emailField, emailError);

        // Add to grid
        int row = 0;
        grid.add(emailLabel, 0, row);
        grid.add(emailBox, 1, row++);

        // Set column constraints
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints(100);
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setFillWidth(true);
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        return grid;
    }

    private HBox createButtonPanel() {
        HBox panel = new HBox(10);
        panel.setStyle("-fx-alignment: center-right;");

        Button submitBtn = new Button("Send");
        submitBtn.setStyle("-fx-padding: 8 24; -fx-font-size: 12;");
        submitBtn.setOnAction(event -> handleForgotPassword());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-padding: 8 24; -fx-font-size: 12;");
        cancelBtn.setOnAction(event -> stage.close());

        panel.getChildren().addAll(submitBtn, cancelBtn);
        return panel;
    }

    private void handleForgotPassword() {
        String email = emailField.getText();

        // Validate fields
        ValidationResult validationResult = AuthValidator.validateForgotPassword(email);

        if (!validationResult.isValid()) {
            displayFieldErrors(validationResult);
            return;
        }

        // Clear errors on success
        clearFieldErrors();

        // Set result values
        resultEmail = email;
        submitted = true;

        // Close dialog
        stage.close();
    }

    private void clearFieldErrors() {
        emailError.setText("");
        emailError.setVisible(false);
        emailError.setManaged(false);
    }

    private void displayFieldErrors(ValidationResult validationResult) {
        clearFieldErrors();

        Map<String, String> fieldErrors = validationResult.getAllFieldErrors();

        // Display email error
        if (fieldErrors.containsKey("email")) {
            emailError.setText(fieldErrors.get("email"));
            emailError.setVisible(true);
            emailError.setManaged(true);
        }
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
     * Get the email entered by user
     */
    public String getEmail() {
        return resultEmail;
    }
}