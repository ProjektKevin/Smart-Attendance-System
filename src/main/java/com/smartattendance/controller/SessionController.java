package com.smartattendance.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// import com.smartattendance.model.Session;
import com.smartattendance.service.SessionService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.repository.StudentRepository;
import com.smartattendance.repository.SessionRepository;
import com.smartattendance.service.SessionService;
import com.smartattendance.util.CheckBoxTableCell;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
<<<<<<< HEAD
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
=======
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
>>>>>>> origin/dev

public class SessionController {
    @FXML
    private Label sessionsInfo;
    @FXML
    private TableView<Session> sessionTable;
    @FXML
<<<<<<< HEAD
=======
    private TableColumn<Session, Boolean> colSelect; 
    @FXML
>>>>>>> origin/dev
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
<<<<<<< HEAD
    TableColumn<Session, Void> colDelete = new TableColumn<>("Actions");

    private final SessionService sm = new SessionService();

    // String sessionId,String courseId, LocalDate sessionDate,
    // LocalTime startTime,LocalTime endTime,String location,
    // int late

    @FXML
    public void initialize() {

=======
    @FXML
    private Button deleteButton;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private CheckBox selectAllCheckBox;

    private final SessionService sm = new SessionService();
    private final ObservableList<Session> sessionList = FXCollections.observableArrayList();
    private final Map<Integer, SimpleBooleanProperty> selectionMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Initialise columns
>>>>>>> origin/dev
        colId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("sessionDate"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colLoc.setCellValueFactory(new PropertyValueFactory<>("location"));
        colLate.setCellValueFactory(new PropertyValueFactory<>("lateThresholdMinutes"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

<<<<<<< HEAD
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
=======
        // Setup checkbox column
        setupCheckBoxColumn();

        // Load sessions from database
        loadSessionsFromDatabase();

        // Add listener to update button states when selection changes
        sessionTable.itemsProperty().addListener((obs, oldItems, newItems) -> updateButtonStates());
        
        // Select All checkbox listener
        if (selectAllCheckBox != null) {
            selectAllCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    selectAllSessions();
                } else {
                    clearAllSelection();
                }
                updateButtonStates();
            });
        }

        // Refresh table and reload data from database periodically
        Timeline uiRefresher = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            loadSessionsFromDatabase();  // Reloads from DB
            sessionTable.refresh();      // Updates the UI
        }));
        uiRefresher.setCycleCount(Timeline.INDEFINITE);
        uiRefresher.play();
    }

    private void setupCheckBoxColumn() {
        colSelect.setCellFactory(col -> new CheckBoxTableCell<>(
            index -> {
                if (index >= 0 && index < sessionList.size()) {
                    Session session = sessionList.get(index);
                    return selectionMap.computeIfAbsent(
                        session.getSessionId(), 
                        k -> new SimpleBooleanProperty(false)
                    ).get();
                }
                return false;
            },
            index -> {
                if (index >= 0 && index < sessionList.size()) {
                    Session session = sessionList.get(index);
                    SimpleBooleanProperty selected = selectionMap.computeIfAbsent(
                        session.getSessionId(), 
                        k -> new SimpleBooleanProperty(false)
                    );
                    selected.set(!selected.get());
                    updateButtonStates();
                }
                return null;
            }
        ));
        
        // Ensures the checkbox column does not try to bind to a property
        colSelect.setCellValueFactory(cellData -> null);
    }

    private void loadSessionsFromDatabase() {
        try {
            List<Session> sessions = sm.getAllSessions();
            boolean statusChanged = false;

            // Update status of sessions based on current time (Automate opening and closing of sessions)  
            for (Session s : sessions){
                String currentStatus = s.getStatus();
                String updatedStatus = s.determineStatus(s.getSessionDate(), s.getStartTime(), s.getEndTime());

                // If status needs to be updated, update in database
                if (!currentStatus.equals(updatedStatus)){
                    s.setStatus(updatedStatus);
                    sm.updateSessionStatus(s);
                    statusChanged = true;
                }
            }

            // Reload from database to get updated status (if necessary)
            if (statusChanged) {
                sessions = sm.getAllSessions();
            }
            
            // Sort sessions by sessionId in ascending order
            sessions.sort(Comparator.comparing(Session::getSessionId));  
            
            sessionList.setAll(sessions);
            sessionTable.setItems(sessionList);
            
            // Clear selection map and repopulate
            selectionMap.clear();
            for (Session session : sessions) {
                selectionMap.put(session.getSessionId(), new SimpleBooleanProperty(false));
            }
            
            sessionsInfo.setText("Loaded " + sessions.size() + " sessions");
            updateButtonStates();
>>>>>>> origin/dev
        } catch (Exception e) {
            sessionsInfo.setText("Error loading sessions: " + e.getMessage());
            e.printStackTrace();
        }
    }
<<<<<<< HEAD
    
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
=======

    // Update button states
    private void updateButtonStates() {
        List<Session> selectedSessions = getSelectedSessions();
        boolean hasSelection = !selectedSessions.isEmpty();
        
        // Update delete button state
        if (deleteButton != null) {
            if (!hasSelection) {
                deleteButton.setDisable(true);
                deleteButton.setTooltip(new Tooltip("Please select session(s) to delete"));
            } else if (selectedSessions.stream().anyMatch(s -> "Open".equals(s.getStatus()))) {
                deleteButton.setDisable(true);
                deleteButton.setTooltip(new Tooltip("Cannot delete open sessions. Stop the session first."));
            } else {
                deleteButton.setDisable(false);
                deleteButton.setTooltip(null);
            }
        }
        
        // Update start button state
        if (startButton != null) {
            if (!hasSelection) {
                startButton.setDisable(true);
                startButton.setTooltip(new Tooltip("Please select session(s) to start"));
            } else if (selectedSessions.stream().anyMatch(s -> !"Pending".equals(s.getStatus()))) {
                startButton.setDisable(true);
                startButton.setTooltip(new Tooltip("Can only start sessions with Pending status"));
            } else {
                startButton.setDisable(false);
                startButton.setTooltip(null);
            }
        }
        
        // Update stop button state
        if (stopButton != null) {
            if (!hasSelection) {
                stopButton.setDisable(true);
                stopButton.setTooltip(new Tooltip("Please select session(s) to stop"));
            } else if (selectedSessions.stream().anyMatch(s -> !"Open".equals(s.getStatus()))) {
                stopButton.setDisable(true);
                stopButton.setTooltip(new Tooltip("Can only stop sessions with Open status"));
            } else {
                stopButton.setDisable(false);
                stopButton.setTooltip(null);
            }
        }
        
        // Update select all checkbox state
        if (selectAllCheckBox != null) {
            selectAllCheckBox.setSelected(hasSelection && selectedSessions.size() == sessionList.size());
        }
    }

    // Get selected sessions
    private List<Session> getSelectedSessions() {
        return sessionList.stream()
                .filter(session -> selectionMap.get(session.getSessionId()).get())
                .collect(Collectors.toList());
    }

    // Select all sessions
    private void selectAllSessions() {
        selectionMap.values().forEach(prop -> prop.set(true));
        sessionTable.refresh();
    }

    // Clear selections
    private void clearAllSelection() {
        selectionMap.values().forEach(prop -> prop.set(false));
        sessionTable.refresh();
    }

    @FXML
    private void onSelectAll() {
        // When select all checkbox is selected
        if (selectAllCheckBox.isSelected()) {
            selectAllSessions();
        } else {
            clearAllSelection();
        }
        updateButtonStates();
    }
>>>>>>> origin/dev

    @FXML
    private void onCreateSession() {
        try {
<<<<<<< HEAD
=======
            // Load the form dialog
>>>>>>> origin/dev
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SessionForm.fxml"));
            Parent form = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Create New Session");
            dialog.setScene(new Scene(form));
<<<<<<< HEAD
            dialog.initModality(Modality.APPLICATION_MODAL); // block interaction with main window
            dialog.showAndWait();

            // After popup closes:
            SessionFormController formCtrl = loader.getController();
            Session newSession = formCtrl.getNewSession();

            if (newSession != null) {
                sessionTable.getItems().add(newSession);
=======
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

            SessionFormController formCtrl = loader.getController();
            Session newSession = formCtrl.getNewSession(); // Get newly created session

            if (newSession != null) {
                loadSessionsFromDatabase(); // Reload to get the new session
>>>>>>> origin/dev
                sessionsInfo.setText("Session " + newSession.getSessionId() + " created successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sessionsInfo.setText("Error creating session: " + e.getMessage());
        }
    }

    @FXML
    private void onStartSession() {
<<<<<<< HEAD
        Session selected = sessionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            sessionsInfo.setText("Please select a session to start.");
            return;
        }

        selected.open();
        sm.updateSessionStatus(selected);
        sessionTable.refresh(); // update table view
        sessionsInfo.setText("Session " + selected.getSessionId() + " is now OPEN.");
=======
        // Selection of sessions to start
        List<Session> selectedSessions = getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            sessionsInfo.setText("Please select session(s) to start.");
            return;
        }

        int successCount = 0;
        for (Session session : selectedSessions) {
            if (!"Open".equals(session.getStatus())) {
                session.open();
                sm.updateSessionStatus(session);
                successCount++;
            }
        }

        sessionTable.refresh();
        clearAllSelection();
        sessionsInfo.setText("Started " + successCount + " session(s) successfully.");
>>>>>>> origin/dev
    }

    @FXML
    private void onStopSession() {
<<<<<<< HEAD
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
=======
        // Selection of sessions to stop if session status != "Closed"
        List<Session> selectedSessions = getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            sessionsInfo.setText("Please select session(s) to stop.");
            return;
        }

        int successCount = 0;
        for (Session session : selectedSessions) {
            if (!"Closed".equals(session.getStatus())) {
                session.close();
                sm.updateSessionStatus(session);
                successCount++;
            }
        }

        sessionTable.refresh();
        clearAllSelection();
        sessionsInfo.setText("Stopped " + successCount + " session(s) successfully.");
    }

    @FXML
    private void onDeleteSession() {
        // Selection of sessions to delete if session status != "Open"
        List<Session> selectedSessions = getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            sessionsInfo.setText("Please select session(s) to delete.");
            return;
        }

        // Check if all sessions are selected and none are open
        boolean shouldDeleteAll = selectedSessions.size() == sessionList.size() && 
                                selectedSessions.stream().noneMatch(s -> "Open".equals(s.getStatus()));

        if (shouldDeleteAll) {
            // Use deleteAll when all sessions are selected
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete All");
            alert.setHeaderText("Delete ALL Sessions");
            alert.setContentText("WARNING: This will permanently delete ALL " + sessionList.size() + " sessions!\n\n" +
                    "This action cannot be undone. Are you absolutely sure?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    SessionRepository repo = new SessionRepository();
                    repo.deleteAll();
                    loadSessionsFromDatabase();
                    sessionsInfo.setText("Successfully deleted all sessions.");
                } catch (Exception e) {
                    sessionsInfo.setText("Error deleting all sessions: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            // Check if any open sessions are selected
            boolean hasOpenSessions = selectedSessions.stream()
                    .anyMatch(s -> "Open".equals(s.getStatus()));

            if (hasOpenSessions) {
                sessionsInfo.setText("Cannot delete open sessions. Stop the sessions first.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete " + selectedSessions.size() + " Session(s)");
            
            String sessionIds = selectedSessions.stream()
                    .map(Session::getSessionId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            
            alert.setContentText("Are you sure you want to delete the selected session(s)?\n" + sessionIds);

            // Use deleteSession when some sessions are selected
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    int successCount = 0;
                    for (Session session : selectedSessions) {
                        sm.deleteSession(session.getSessionId());
                        successCount++;
                    }
                    
                    loadSessionsFromDatabase();
                    sessionsInfo.setText("Successfully deleted " + successCount + " session(s).");
                } catch (Exception e) {
                    sessionsInfo.setText("Error deleting sessions: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}

// if there is error, alert using notification bar instead of SessionInfo (currently, tooltips cannot work)
// Cannot remove the selection effect?
// when course entered does not have any student enrolled, don't allow to create?
// expand to see student roster (Attendance Record)
// add user_id
>>>>>>> origin/dev
