package com.smartattendance.controller; import javafx.fxml.FXML; import javafx.scene.control.Label;
public class SessionController { @FXML private Label sessionsInfo; @FXML public void initialize(){ sessionsInfo.setText("No sessions created (scaffold)."); }
  @FXML private void onCreateSession(){ sessionsInfo.setText("Created session CS102 "+System.currentTimeMillis()+" (stub)."); }
  @FXML private void onStartSession(){ sessionsInfo.setText("Session started (stub)."); }
  @FXML private void onStopSession(){ sessionsInfo.setText("Session stopped (stub)."); } }
