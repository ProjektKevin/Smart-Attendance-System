package com.smartattendance;

import com.smartattendance.util.LoggerUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class MainApp extends Application {

  @Override
  public void init() throws Exception {
    // Initialize ApplicationContext before UI loads
    ApplicationContext.initialize();
    LoggerUtil.LOGGER.info("Application Context loaded");
  }

  @Override
  public void start(Stage stage) throws Exception {
    Parent loginUI = FXMLLoader.load(getClass().getResource("/view/LoginView.fxml"));
    stage.setTitle("Smart Attendance System - Login");
    stage.setScene(new Scene(loginUI));
    stage.show();
    LoggerUtil.LOGGER.info("App started");
  }

  @Override
  public void stop() throws Exception {
    // Cleanup when application closes
    ApplicationContext.shutdown();
    LoggerUtil.LOGGER.info("App stopped");
  }

  public static void main(String[] args) {
    launch(args);
  }
}
