package com.smartattendance.controller;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class SettingsController {
  @FXML
  private TextField cameraIndexField, highThresholdField, lowThresholdField, cooldownField, dbPathField;

  @FXML
  private ChoiceBox<String> algorithmChoiceBox;

  @FXML
  private Label statusLabel, imageAmountLabel;

  @FXML
  private Slider imageAmountSlider;

  @FXML
  public void initialize() {
    cameraIndexField.setText(String.valueOf(Config.get("camera.index")));
    highThresholdField.setText(String.valueOf(Config.get("recognition.high.threshold")));
    lowThresholdField.setText(String.valueOf(Config.get("recognition.low.threshold")));
    cooldownField.setText(String.valueOf(Config.get("cooldown.seconds")));
    dbPathField.setText(String.valueOf(Config.get("database.path")));
    algorithmChoiceBox.getItems().addAll("HISTOGRAM", "OPENFACE");

    String imageAmountStr = Config.get("enrollment.image.amount");
    int imageAmount = (imageAmountStr != null) ? Integer.parseInt(imageAmountStr) : 20;

    imageAmountSlider.setValue(imageAmount);
    imageAmountLabel.setText(String.valueOf(imageAmount));

    imageAmountSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
      int val = newVal.intValue();
      imageAmountLabel.setText(String.valueOf(val));
    });

    String currentAlgorithm = Config.get("recognition.algorithm");
    if (currentAlgorithm != null) {
      algorithmChoiceBox.setValue(currentAlgorithm);
    } else {
      algorithmChoiceBox.setValue("HISTOGRAM");
    }

    setupStatusClear(cameraIndexField);
    setupStatusClear(highThresholdField);
    setupStatusClear(lowThresholdField);
    setupStatusClear(cooldownField);
    setupStatusClear(dbPathField);
    setupStatusClear(algorithmChoiceBox);
    setupStatusClear(imageAmountSlider);

  }

  @FXML
  private void onSaveSettings() {
    try {
      // Validate and save threshold
      double highThreshold = Double.parseDouble(highThresholdField.getText());
      double lowThreshold = Double.parseDouble(lowThresholdField.getText());
      int cooldown = Integer.parseInt(cooldownField.getText());
      int imageAmount = (int) imageAmountSlider.getValue();

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

      if (imageAmount < 10 || imageAmount > 25) {
        imageAmountSlider.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        statusLabel
            .setText("Error: Enrollment image amount must be between 10 and 25 (You entered: " + imageAmount + ")");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        return;
      } else {
        imageAmountSlider.setStyle(""); // reset style
      }

      // Save settings to config (except algorithm - handled by switchAlgorithm)
      Config.set("camera.index", cameraIndexField.getText());
      Config.set("recognition.high.threshold", String.valueOf(highThreshold));
      Config.set("recognition.low.threshold", String.valueOf(lowThreshold));
      Config.set("cooldown.seconds", String.valueOf(cooldown));
      Config.set("database.path", dbPathField.getText());
      Config.set("enrollment.image.amount", String.valueOf(imageAmount));

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

      statusLabel.setText("New Settings saved! ");
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

  private void setupStatusClear(Control control) {
    Runnable clearStatus = () -> statusLabel.setText("");

    if (control instanceof TextField tf) {
      tf.setOnMouseClicked(e -> clearStatus.run());
      tf.setOnKeyTyped(e -> clearStatus.run());
    } else if (control instanceof ChoiceBox<?> cb) {
      cb.setOnMouseClicked(e -> clearStatus.run());
      cb.setOnAction(e -> clearStatus.run());
    } else if (control instanceof Slider s) {
      s.setOnMousePressed(e -> clearStatus.run());
      s.valueProperty().addListener((obs, oldVal, newVal) -> clearStatus.run());
    }
  }

}
