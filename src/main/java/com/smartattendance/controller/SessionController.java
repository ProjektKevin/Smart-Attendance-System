package com.smartattendance.controller;

import com.smartattendance.model.Session;
import com.smartattendance.repository.SessionRepository;
import com.smartattendance.service.SessionService;
import com.smartattendance.util.CheckBoxTableCell;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

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
        // Initialize columns
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

        // Refresh table periodically
        Timeline uiRefresher = new Timeline(new KeyFrame(Duration.seconds(30), e -> sessionTable.refresh()));
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
        
        // Ensures the checkbox column doesn't try to bind to a property
        colSelect.setCellValueFactory(cellData -> null);
    }

    private void loadSessionsFromDatabase() {
        try {
            List<Session> sessions = sm.getAllSessions();
            
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

    private List<Session> getSelectedSessions() {
        return sessionList.stream()
                .filter(session -> selectionMap.get(session.getSessionId()).get())
                .collect(Collectors.toList());
    }

    private void selectAllSessions() {
        selectionMap.values().forEach(prop -> prop.set(true));
        sessionTable.refresh();
    }

    private void clearAllSelection() {
        selectionMap.values().forEach(prop -> prop.set(false));
        sessionTable.refresh();
    }

    @FXML
    private void onSelectAll() {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SessionForm.fxml"));
            Parent form = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Create New Session");
            dialog.setScene(new Scene(form));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

            SessionFormController formCtrl = loader.getController();
            Session newSession = formCtrl.getNewSession();

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
        List<Session> selectedSessions = getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            sessionsInfo.setText("Please select session(s) to delete.");
            return;
        }

        // Check if all sessions are selected and none are open
        boolean shouldDeleteAll = selectedSessions.size() == sessionList.size() && 
                                selectedSessions.stream().noneMatch(s -> "Open".equals(s.getStatus()));

        if (shouldDeleteAll) {
            // Use deleteAll for better performance
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
            // Original logic for individual deletions
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
// when session is fetched, check if status is correct, if not update
// expand to see student roster (Attendance Record)
// automate open and closing of sessions
// add user_id