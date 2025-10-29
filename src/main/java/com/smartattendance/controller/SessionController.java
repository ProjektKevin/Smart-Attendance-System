package com.smartattendance.controller;

import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.repository.StudentRepository;
import com.smartattendance.service.SessionService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.table.TableRowExpanderColumn;

import java.time.*;
import java.util.List;

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
    @FXML
    private TableColumn<Session, String> colStatus;
    TableColumn<Session, Void> colDelete = new TableColumn<>("Actions");

    private final SessionService sm = new SessionService();

    // String sessionId,String courseId, LocalDate sessionDate,
    // LocalTime startTime,LocalTime endTime,String location,
    // int late

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("sessionDate"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colLoc.setCellValueFactory(new PropertyValueFactory<>("location"));
        colLate.setCellValueFactory(new PropertyValueFactory<>("lateThresholdMinutes"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Load sessions from database
        loadSessionsFromDatabase();

        // Refresh table periodically (optional, for UI updates)
        Timeline uiRefresher = new Timeline(new KeyFrame(Duration.seconds(30), e -> sessionTable.refresh()));
        uiRefresher.setCycleCount(Timeline.INDEFINITE);
        uiRefresher.play();

    } 

    private void loadSessionsFromDatabase() {
        try {
            List<Session> sessions = sm.getAllSessions(); 
            sessionTable.getItems().setAll(sessions); 
            sessionsInfo.setText("Loaded " + sessions.size() + " sessions");
        } catch (Exception e) {
            sessionsInfo.setText("Error loading sessions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // change gui so there is checkboxes for users to select one or multiple sessions and then can either click open/stop/delete
    // if there is error, alert using notification bar instead of SessionInfo
    // when session created is already closed, then don't allow to create?
    // when course entered does not have any student enrolled, don't allow to create?
    // when session is fetched, check if status is correct, if not update
    // allow clear all --> make sure index resets once deleted all (also add double check dialog)
    // delete button 
    // expand to see student roster (Attendance Record)
    // automate open and closing of sessions
    // add user_id

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
        Session selected = sessionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            sessionsInfo.setText("Please select a session to start.");
            return;
        }

        selected.open();
        sm.updateSessionStatus(selected);
        sessionTable.refresh(); // update table view
        sessionsInfo.setText("Session " + selected.getSessionId() + " is now OPEN.");
    }

    @FXML
    private void onStopSession() {
        Session selected = sessionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            sessionsInfo.setText("Please select a session to stop.");
            return;
        }

        selected.close();
        sm.updateSessionStatus(selected);
        sessionTable.refresh();
        sessionsInfo.setText("Session " + selected.getSessionId() + " has been CLOSED.");
    }

    // @FXML
    // private void onDeleteSession(){
    //     Session selected = sessionTable.getSelectionModel().getSelectedItem();
    //     if (selected == null) {
    //         sessionsInfo.setText("Please select a session to delete.");
    //         return;
    //     }

    //     sm.deleteSession(selected.getSessionId());
    //     sessionTable.refresh();
    //     sessionsInfo.setText("Session " + selected.getSessionId() + " has been CLOSED.");
    // }
}