package com.smartattendance.controller; import com.smartattendance.util.Config; import javafx.fxml.FXML; import javafx.scene.control.TextField;
public class SettingsController {
  @FXML private TextField cameraIndexField, thresholdField, lateField, dbPathField;
  @FXML public void initialize(){ cameraIndexField.setText(String.valueOf(Config.get("camera.index"))); thresholdField.setText(String.valueOf(Config.get("recognition.threshold")));
    lateField.setText(String.valueOf(Config.get("late.threshold.minutes"))); dbPathField.setText(String.valueOf(Config.get("database.path"))); }
  @FXML private void onSaveSettings(){ System.out.println("Settings saved (stub): "+cameraIndexField.getText()+", "+thresholdField.getText()+", "+lateField.getText()+", "+dbPathField.getText()); }
}
