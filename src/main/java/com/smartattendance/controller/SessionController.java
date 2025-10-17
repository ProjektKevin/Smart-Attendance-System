package com.smartattendance.controller;

import com.smartattendance.model.Session;
import com.smartattendance.model.Student;
import com.smartattendance.service.SessionService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.*;

public class SessionController {
  @FXML
  private Label sessionsInfo;
  @FXML
  private TableView<Session> sessionTable;
  @FXML 
  private TableColumn<Session, String> colId;
  @FXML
  private TableColumn<Session, String> colCourse;
  @FXML
  private TableColumn<Session, LocalDate> colDate;
  @FXML
  private TableColumn<Session, LocalTime> colStart;
  @FXML
  private TableColumn<Session, LocalTime> colEnd;
  @FXML
  private TableColumn<Session, String> colLoc;
  @FXML
  private TableColumn<Session, Integer> colLate;
  private final SessionService sm = new SessionService();

  // String sessionId,String courseId, LocalDate sessionDate,
  // LocalTime startTime,LocalTime endTime,String location,
  // int late

  @FXML
  public void initialize() {
    colId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
    colCourse.setCellValueFactory(new PropertyValueFactory<>("courseId"));
    colDate.setCellValueFactory(new PropertyValueFactory<>("sessionDate"));
    colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
    colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
    colLoc.setCellValueFactory(new PropertyValueFactory<>("location"));
    colLate.setCellValueFactory(new PropertyValueFactory<>("lateThresholdMinutes"));
    ObservableList<Session> data = FXCollections.observableArrayList(sm.getAllSessions());
    sessionTable.setItems(data);
  }

  @FXML
  private void onCreateSession() {
      try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SessionForm.fxml"));
          Parent form = loader.load();

          Stage dialog = new Stage();
          dialog.setTitle("Create New Session");
          dialog.setScene(new Scene(form));
          dialog.initModality(Modality.APPLICATION_MODAL); // block interaction with main window
          dialog.showAndWait();

          // After popup closes:
          SessionFormController formCtrl = loader.getController();
          Session newSession = formCtrl.getNewSession();

          if (newSession != null) {
              sm.createSession(newSession); 
              sessionTable.getItems().add(newSession);
              sessionsInfo.setText("Session " + newSession.getSessionId() + " created successfully!");
          }

      } catch (Exception e) {
          e.printStackTrace();
          sessionsInfo.setText("Error creating session: " + e.getMessage());
      }
  }

  @FXML
  private void onStartSession() {
    sessionsInfo.setText("Session started (stub).");
  }

  @FXML
  private void onStopSession() {
    sessionsInfo.setText("Session stopped (stub).");
  }
}