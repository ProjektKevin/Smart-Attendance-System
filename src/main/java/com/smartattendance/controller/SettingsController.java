package com.smartattendance.controller;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SettingsController {
  @FXML
  private TextField cameraIndexField, thresholdField, /* lateField ,*/ cooldownField, dbPathField;

  @FXML
  private ChoiceBox<String> algorithmChoiceBox;

  @FXML
  private Label statusLabel;

  @FXML
  public void initialize() {
    cameraIndexField.setText(String.valueOf(Config.get("camera.index")));
    thresholdField.setText(String.valueOf(Config.get("recognition.threshold")));
    // lateField.setText(String.valueOf(Config.get("late.threshold.minutes")));
    cooldownField.setText(String.valueOf(Config.get("cooldown.seconds")));
    dbPathField.setText(String.valueOf(Config.get("database.path")));
    algorithmChoiceBox.getItems().addAll("HISTOGRAM", "OPENFACE");
    String currentAlgorithm = Config.get("recognition.algorithm");
    if (currentAlgorithm != null) {
      algorithmChoiceBox.setValue(currentAlgorithm);
    } else {
      algorithmChoiceBox.setValue("HISTOGRAM");
    }
  }

  @FXML
  private void onSaveSettings() {
    try {
      // Validate and save threshold
      double threshold = Double.parseDouble(thresholdField.getText());

      if (threshold < 0 || threshold > 100) {
        System.out.println("Threshold must be between 0 and 100");
        thresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        statusLabel.setText("Error: Threshold must be between 0 and 100 (You entered: " + threshold + ")");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        return;
      }

      // Save settings to config (except algorithm - handled by switchAlgorithm)
      Config.set("camera.index", cameraIndexField.getText());
      Config.set("recognition.threshold", String.valueOf(threshold));
      // Config.set("late.threshold.minutes", lateField.getText());
      Config.set("cooldown.seconds", cooldownField.getText());
      Config.set("database.path", dbPathField.getText());

      // Update threshold dynamically
      ApplicationContext.getHistogramRecognizer().setConfidenceThreshold(threshold);
      if (ApplicationContext.getOpenFaceRecognizer() != null) {
        ApplicationContext.getOpenFaceRecognizer().setConfidenceThreshold(threshold);
      }

      // Switch algorithm (this also saves to config)
      String selectedAlgorithm = algorithmChoiceBox.getValue();
      ApplicationContext.getFaceRecognitionService().switchAlgorithm(selectedAlgorithm);

      System.out.println("Settings saved successfully");
      thresholdField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");

      statusLabel.setText("Settings saved successfully! Algorithm: " + selectedAlgorithm + ", Threshold: " + threshold + "%");
      statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");


    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      thresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

      statusLabel.setText("Error: Please enter a valid number for threshold (0-100)");
      statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }
  }
}
