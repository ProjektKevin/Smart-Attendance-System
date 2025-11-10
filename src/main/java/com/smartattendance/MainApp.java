package com.smartattendance;

import com.smartattendance.util.security.log.ApplicationLogger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class MainApp extends Application {

  @Override
  public void init() throws Exception {
    // Initialize ApplicationContext before UI loads
    ApplicationContext.initialize();
    ApplicationLogger.getInstance().info("Application Context loaded");
  }

  @Override
  public void start(Stage stage) throws Exception {
    Parent loginUI = FXMLLoader.load(getClass().getResource("/view/LoginView.fxml"));
    stage.setTitle("Smart Attendance System - Login");
    stage.setScene(new Scene(loginUI));
    stage.show();
    ApplicationLogger.getInstance().info("App started");
  }

  @Override
  public void stop() throws Exception {
    // Cleanup when application closes
    ApplicationContext.shutdown();
    ApplicationLogger.getInstance().info("App stopped");
  }

  public static void main(String[] args) {
    launch(args);
  }
}
