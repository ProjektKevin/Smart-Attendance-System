package com.smartattendance.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.smartattendance.model.entity.Session;
import com.smartattendance.repository.SessionRepository;
import com.smartattendance.service.SessionService;
import com.smartattendance.util.CheckBoxTableCell;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SessionController {
    @FXML
    private Label sessionsInfo;
    @FXML
    private TableView<Session> sessionTable;
    @FXML
    private TableColumn<Session, Boolean> colSelect; 
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
    @FXML
    private TableColumn<Session, Void> colViewMore;
    @FXML
    private Button deleteButton;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private CheckBox selectAllCheckBox;
    @FXML 
    private VBox sessionListContainer;
    @FXML 
    private VBox attendanceViewContainer;

    private final SessionService sm = new SessionService();
    private final ObservableList<Session> sessionList = FXCollections.observableArrayList();
    private final Map<Integer, SimpleBooleanProperty> selectionMap = new HashMap<>();

    @FXML
    public void initialize() {
        attendanceViewContainer.setVisible(false);
        attendanceViewContainer.setManaged(false);
        
        // Initialise columns
        colId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("sessionDate"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colLoc.setCellValueFactory(new PropertyValueFactory<>("location"));
        colLate.setCellValueFactory(new PropertyValueFactory<>("lateThresholdMinutes"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup checkbox column
        setupCheckBoxColumn();

        // Setup view more button 
        setUpViewMoreButton();

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

    private void setUpViewMoreButton() {
        colViewMore.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View More");

            {
                btn.setOnAction(event -> {
                    Session session = getTableView().getItems().get(getIndex());
                    openAttendancePage(session);
                });
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void openAttendancePage(Session session) {
        try {
            // Load the Attendance view fresh and get its controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AttendanceView.fxml"));
            Parent attendanceRoot = loader.load();
            AttendanceController attendanceCtrl = loader.getController();

            // Pass session to the attendance controller
            attendanceCtrl.setSession(session);

            // Provide a callback so AttendanceController can go back to the sessions list
            attendanceCtrl.setBackHandler(() -> {
                // hide attendance view and show session list
                attendanceViewContainer.getChildren().clear();
                attendanceViewContainer.setVisible(false);
                attendanceViewContainer.setManaged(false);

                sessionListContainer.setVisible(true);
                sessionListContainer.setManaged(true);
            });

            // Hide session list
            sessionListContainer.setVisible(false);
            sessionListContainer.setManaged(false);

            // Put attendance UI into placeholder and show it
            attendanceViewContainer.getChildren().setAll(attendanceRoot);
            attendanceViewContainer.setVisible(true);
            attendanceViewContainer.setManaged(true);

        } catch (Exception e) {
            e.printStackTrace();
            sessionsInfo.setText("Error opening attendance page: " + e.getMessage());
        }
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
        } catch (Exception e) {
            sessionsInfo.setText("Error loading sessions: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    @FXML
    private void onCreateSession() {
        try {
            // Load the form dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SessionForm.fxml"));
            Parent form = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Create New Session");
            dialog.setScene(new Scene(form));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

            SessionFormController formCtrl = loader.getController();
            Session newSession = formCtrl.getNewSession(); // Get newly created session

            if (newSession != null) {
                loadSessionsFromDatabase(); // Reload to get the new session
                sessionsInfo.setText("Session " + newSession.getSessionId() + " created successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sessionsInfo.setText("Error creating session: " + e.getMessage());
        }
    }

    @FXML
    private void onStartSession() {
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
    }

    @FXML
    private void onStopSession() {
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
// Cannot remove the selection effect? Should I also remove the automation of opening/closing of sessions?
// when course entered does not have any student enrolled, don't allow to create?
// expand to see student roster (Attendance Record)?
// implement edit session function?
// add user_id so view for each type of user is different
// implement gui for attendance record under each session (why the tabs and everything missing? also test select one session update status / delete)
