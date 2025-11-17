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
 * Dialog box to ask for token verification
 * 
 * @author Thiha Swan Htet
 */
public class VerificationDialog {
    private final Stage stage;
    private boolean submitted = false;
    private String resultTokenInput;

    // UI Components
    private TextField tokenInputField;
    private Label tokenInputError;
    private Label statusLabel;

    public VerificationDialog() {
        this.stage = new Stage();
        initializeDialog();
    }

    private void initializeDialog() {
        // Set up stage properties
        stage.setTitle("Verification");
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
        Label titleLabel = new Label("Verification");
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

        // Token Field
        Label tokenFieldLabel = new Label("Token:");
        tokenFieldLabel.setStyle("-fx-font-weight: bold;");
        tokenInputField = new TextField();
        tokenInputField.setPromptText("Enter verification token");
        tokenInputField.setStyle("-fx-font-size: 12; -fx-padding: 8;");

        tokenInputError = new Label("");
        tokenInputError.setStyle("-fx-font-size: 11; -fx-text-fill: #d32f2f;");
        tokenInputError.setVisible(false);
        tokenInputError.setManaged(false);
        tokenInputError.setWrapText(true);

        VBox tokenInputBox = new VBox(4);
        tokenInputBox.getChildren().addAll(tokenInputField, tokenInputError);

        // Add to grid
        int row = 0;
        grid.add(tokenFieldLabel, 0, row);
        grid.add(tokenInputBox, 1, row++);

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

        Button submitBtn = new Button("Submit");
        submitBtn.setStyle("-fx-padding: 8 24; -fx-font-size: 12;");
        submitBtn.setOnAction(event -> handleVerification());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-padding: 8 24; -fx-font-size: 12;");
        cancelBtn.setOnAction(event -> stage.close());

        panel.getChildren().addAll(submitBtn, cancelBtn);
        return panel;
    }

    private void handleVerification() {
        String token = tokenInputField.getText();

        // Validate fields
        ValidationResult validationResult = AuthValidator.validateVerification(token);

        if (!validationResult.isValid()) {
            displayFieldErrors(validationResult);
            return;
        }

        // Clear errors on success
        clearFieldErrors();

        // Set result values
        resultTokenInput = token;
        submitted = true;

        // Close dialog
        stage.close();
    }

    private void clearFieldErrors() {
        tokenInputError.setText("");
        tokenInputError.setVisible(false);
        tokenInputError.setManaged(false);
    }

    private void displayFieldErrors(ValidationResult validationResult) {
        clearFieldErrors();

        Map<String, String> fieldErrors = validationResult.getAllFieldErrors();

        // Display token error
        if (fieldErrors.containsKey("token")) {
            tokenInputError.setText(fieldErrors.get("token"));
            tokenInputError.setVisible(true);
            tokenInputError.setManaged(true);
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
     * Get the token entered by user
     */
    public String getToken() {
        return resultTokenInput;
    }
}
