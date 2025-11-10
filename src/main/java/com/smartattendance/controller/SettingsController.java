package com.smartattendance.controller;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class SettingsController {
  @FXML
  private TextField cameraIndexField, thresholdField, lateField, cooldownField, dbPathField;

  @FXML
  private ChoiceBox<String> algorithmChoiceBox;

  @FXML
  public void initialize() {
    cameraIndexField.setText(String.valueOf(Config.get("camera.index")));
    thresholdField.setText(String.valueOf(Config.get("recognition.threshold")));
    lateField.setText(String.valueOf(Config.get("late.threshold.minutes")));
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
    // Save algorithm setting
    String selectedAlgorithm = algorithmChoiceBox.getValue();
    Config.set("recognition.algorithm", selectedAlgorithm);

    // Apply the change immediately
    ApplicationContext.applyRecognitionAlgorithm();

    System.out.println("Settings saved (stub): " + cameraIndexField.getText() + ", " + thresholdField.getText() + ", "
        + lateField.getText() + ", " + dbPathField.getText());
  }
}
