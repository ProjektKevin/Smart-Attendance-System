package com.smartattendance.controller;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;
import com.smartattendance.util.CameraUtils;
import com.smartattendance.util.security.log.ApplicationLogger;
import com.smartattendance.util.validation.ConfigValidator;
import com.smartattendance.util.validation.ValidationResult;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class SettingsController {
  @FXML
  private TextField highThresholdField, lowThresholdField, cooldownField, dbPathField;

  @FXML
  private ChoiceBox<String> algorithmChoiceBox;

  @FXML
  private ChoiceBox<Integer> cameraIndexChoiceBox;

  @FXML
  private Label statusLabel, imageAmountLabel;

  @FXML
  private Slider imageAmountSlider;

  private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

  @FXML
  public void initialize() {
    // Detect available camera indexs
    int[] availableCameras = CameraUtils.getAvailableCameras();
    for (int cameraIndex : availableCameras) {
      cameraIndexChoiceBox.getItems().add(cameraIndex);
    }

    // Set current camera index from config
    String currentCameraIndexStr = Config.get("camera.index");
    if (currentCameraIndexStr != null) {
      try {
        int currentCameraIndex = Integer.parseInt(currentCameraIndexStr);
        cameraIndexChoiceBox.setValue(currentCameraIndex);
      } catch (NumberFormatException e) {
        // If parsing fails or camera not in list, select first available camera
        if (availableCameras.length != 0) {
          cameraIndexChoiceBox.setValue(availableCameras[0]);
        }
      }
    } else if (availableCameras.length > 0) {
      // Default to first available camera if no config exists
      cameraIndexChoiceBox.setValue(availableCameras[0]);
    }

    // If no cameras detected, add a placeholder
    if (availableCameras.length == 0) {
      cameraIndexChoiceBox.getItems().add(-1);
      cameraIndexChoiceBox.setValue(-1);
      appLogger.warn("No cameras detected!");
    }

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

    setupStatusClear(cameraIndexChoiceBox);
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
      // Clear all field styles first, just in case
      clearFieldStyles();

      // Get field values
      String cameraIndex = String.valueOf(cameraIndexChoiceBox.getValue());
      String highThreshold = highThresholdField.getText();
      String lowThreshold = lowThresholdField.getText();
      String cooldown = cooldownField.getText();
      String databasePath = dbPathField.getText();
      String imageAmount = String.valueOf((int) imageAmountSlider.getValue());

      // Validate using ConfigValidator
      ValidationResult validationResult = ConfigValidator.validateConfig(
          cameraIndex,
          highThreshold,
          lowThreshold,
          cooldown,
          databasePath);

      // Check if validation passed
      if (!validationResult.isValid()) {
        handleValidationErrors(validationResult);
        return;
      }

      // Save settings to config (except algorithm - handled by switchAlgorithm)
      Config.set("camera.index", cameraIndex);
      Config.set("recognition.high.threshold", String.valueOf(highThreshold));
      Config.set("recognition.low.threshold", String.valueOf(lowThreshold));
      Config.set("cooldown.seconds", cooldown);
      Config.set("database.path", dbPathField.getText());
      Config.set("enrollment.image.amount", String.valueOf(imageAmount));

      // Update threshold dynamically
      ApplicationContext.getHistogramRecognizer().setConfidenceThreshold(Double.parseDouble(highThreshold));
      if (ApplicationContext.getOpenFaceRecognizer() != null) {
        ApplicationContext.getOpenFaceRecognizer().setConfidenceThreshold(Double.parseDouble(highThreshold));
      }

      // Switch algorithm (this also saves to config)
      String selectedAlgorithm = algorithmChoiceBox.getValue();
      ApplicationContext.getFaceRecognitionService().switchAlgorithm(selectedAlgorithm);

      // Show sucessfull
      appLogger.info("Settings saved successfully");

      statusLabel.setText("New Settings saved! ");
      statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    } catch (Exception e) {
      appLogger.error("Error: " + e.getMessage());
      e.printStackTrace();

      statusLabel.setText("Error: Failed to save settings");
      statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }
  }

  private void clearFieldStyles() {
    cameraIndexChoiceBox.setStyle("");
    highThresholdField.setStyle("");
    lowThresholdField.setStyle("");
    cooldownField.setStyle("");
    imageAmountSlider.setStyle("");
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

  // Method to handle all the validation errors
  private void handleValidationErrors(ValidationResult result) {
    // Set border color for fields with errors
    if (result.getFieldError("cameraIndex") != null) {
      cameraIndexChoiceBox.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
    }
    
    if (result.getFieldError("highThreshold") != null) {
      highThresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
    }

    if (result.getFieldError("lowThreshold") != null) {
      lowThresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
    }

    if (result.getFieldError("thresholds") != null) {
      highThresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
      lowThresholdField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
    }

    if (result.getFieldError("cooldown") != null) {
      cooldownField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
    }

    if (result.getFieldError("imageAmount") != null) {
      imageAmountSlider.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
    }

    // Display the first error message in status label
    String firstError = result.getAllFieldErrors().values().iterator().next();
    statusLabel.setText("Error: " + firstError);
    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
  }
}
