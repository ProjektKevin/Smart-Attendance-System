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
  private TextField cameraIndexField, highThresholdField, lowThresholdField, cooldownField, dbPathField;

  @FXML
  private ChoiceBox<String> algorithmChoiceBox;

  @FXML
  private Label statusLabel;

  @FXML
  public void initialize() {
    cameraIndexField.setText(String.valueOf(Config.get("camera.index")));
    highThresholdField.setText(String.valueOf(Config.get("recognition.high.threshold")));
    lowThresholdField.setText(String.valueOf(Config.get("recognition.low.threshold")));
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
      double highThreshold = Double.parseDouble(highThresholdField.getText());
      double lowThreshold = Double.parseDouble(lowThresholdField.getText());
      int cooldown = Integer.parseInt(cooldownField.getText());

      if (highThreshold < 0 || highThreshold > 100) {
        highThresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        statusLabel.setText("Error: High threshold must be between 0 and 100 (You entered: " + highThreshold + ")");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        return;
      }

      if (lowThreshold < 0 || lowThreshold > 100) {
        lowThresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        statusLabel.setText("Error: Low threshold must be between 0 and 100 (You entered: " + lowThreshold + ")");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        return;
      }

      if (highThreshold <= lowThreshold || highThreshold - lowThreshold < 1.0) {
        highThresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        lowThresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        statusLabel
            .setText("Error: High threshold must be greater than low threshold and must be at least one point apart!");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        return;
      }

      if (cooldown < 0) {
        cooldownField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        statusLabel.setText("Error: Cooldown must be 0 or greater (You entered: " + cooldown + ")");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        return;
      }

      // Save settings to config (except algorithm - handled by switchAlgorithm)
      Config.set("camera.index", cameraIndexField.getText());
      Config.set("recognition.high.threshold", String.valueOf(highThreshold));
      Config.set("recognition.low.threshold", String.valueOf(lowThreshold));
      Config.set("cooldown.seconds", String.valueOf(cooldown)); 
      Config.set("database.path", dbPathField.getText());

      // Update threshold dynamically
      ApplicationContext.getHistogramRecognizer().setConfidenceThreshold(highThreshold);
      if (ApplicationContext.getOpenFaceRecognizer() != null) {
        ApplicationContext.getOpenFaceRecognizer().setConfidenceThreshold(highThreshold);
      }

      // Switch algorithm (this also saves to config)
      String selectedAlgorithm = algorithmChoiceBox.getValue();
      ApplicationContext.getFaceRecognitionService().switchAlgorithm(selectedAlgorithm);

      // Show sucessfull
      System.out.println("Settings saved successfully");
      highThresholdField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
      lowThresholdField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
      cooldownField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");

      statusLabel.setText("✅ Settings saved! Algorithm: " + selectedAlgorithm +
          " | High: " + highThreshold + "% | Low: " + lowThreshold + "%");
      statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    } catch (NumberFormatException e) {
      // Handle invalid number format
      System.err.println("Error: Invalid number format");
      highThresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
      lowThresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

      statusLabel.setText("Error: Please enter valid numbers for thresholds (0-100)");
      statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();

      statusLabel.setText("Error: Failed to save settings");
      statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }
  }
}
