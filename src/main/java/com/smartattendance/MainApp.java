package com.smartattendance;

import com.smartattendance.util.LoggerUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class MainApp extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    Parent loginUI = FXMLLoader.load(getClass().getResource("/view/LoginView.fxml"));
    stage.setTitle("Smart Attendance System - Login");
    stage.setScene(new Scene(loginUI));
    stage.show();
    LoggerUtil.LOGGER.info("App started");
  }

  public static void main(String[] args) {
    launch(args);
  }
}
