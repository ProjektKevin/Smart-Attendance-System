package com.smartattendance.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.Session;
import com.smartattendance.service.SessionService;
import com.smartattendance.util.CheckBoxTableCell;
import com.smartattendance.util.ControllerRegistry;

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
import javafx.scene.control.ToggleButton;
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
    private TableColumn<Session, Boolean> colAutoStart;
    @FXML
    private TableColumn<Session, Boolean> colAutoStop;
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

    private final SessionService ss = new SessionService();
    private final ObservableList<Session> sessionList = FXCollections.observableArrayList();
    private final Map<Integer, SimpleBooleanProperty> selectionMap = new HashMap<>();
    // F_MA: added by felicia handling marking attendance
    private AttendanceController attendanceController; // store reference

    @FXML
    public void initialize() {
        // Apply initial styling to the info label
        styleInfoLabel("normal", "Loaded sessions will appear here");

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
        colAutoStart.setCellValueFactory(new PropertyValueFactory<>("autoStart"));
        colAutoStop.setCellValueFactory(new PropertyValueFactory<>("autoStop"));

        // Setup checkbox column
        setupCheckBoxColumn();

        // Setup auto start column
        setupAutoStartColumn();

        // Setup auto stop column
        setupAutoStopColumn();

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

        // Auto session processor - runs every 30 seconds
        Timeline autoSessionProcessor = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            // System.out.println("SessionController: Auto-session processor triggered
            // (30-second interval)");

            // Uses decorator pattern rules
            ss.processAutoSessions();

            // Refresh the table to show any status changes
            loadSessionsFromDatabase();
        }));

        autoSessionProcessor.setCycleCount(Timeline.INDEFINITE);
        autoSessionProcessor.play();
    }

    public AttendanceController getAttendanceController() {
        return attendanceController;
    }

    // Styles the info label based on message type
    // @param type "success", "error", "warning", or "normal"
    // @param message The text to display
    private void styleInfoLabel(String type, String message) {
        sessionsInfo.setText(message);

        // Reset styles first
        sessionsInfo.setStyle("-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        switch (type.toLowerCase()) {
            case "success":
                sessionsInfo
                        .setStyle("-fx-text-fill: #155724; -fx-background-color: #d4edda; -fx-border-color: #c3e6cb; " +
                                "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
            case "error":
                sessionsInfo
                        .setStyle("-fx-text-fill: #721c24; -fx-background-color: #f8d7da; -fx-border-color: #f5c6cb; " +
                                "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
            case "warning":
                sessionsInfo
                        .setStyle("-fx-text-fill: #856404; -fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; " +
                                "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
            case "normal":
            default:
                sessionsInfo
                        .setStyle("-fx-text-fill: #383d41; -fx-background-color: #e2e3e5; -fx-border-color: #d6d8db; " +
                                "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
        }
    }

    private void setupCheckBoxColumn() {
        colSelect.setCellFactory(col -> new CheckBoxTableCell<>(
                index -> {
                    if (index >= 0 && index < sessionList.size()) {
                        Session session = sessionList.get(index);
                        return selectionMap.computeIfAbsent(
                                session.getSessionId(),
                                k -> new SimpleBooleanProperty(false)).get();
                    }
                    return false;
                },
                index -> {
                    if (index >= 0 && index < sessionList.size()) {
                        Session session = sessionList.get(index);
                        SimpleBooleanProperty selected = selectionMap.computeIfAbsent(
                                session.getSessionId(),
                                k -> new SimpleBooleanProperty(false));
                        selected.set(!selected.get());
                        updateButtonStates();
                    }
                    return null;
                }));

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
            // F_MA: modified by felicia handling marking attendance
            attendanceController = loader.getController();
            // AttendanceController attendanceCtrl = loader.getController();
            // Save globally for AutoAttendanceUpdater to access
            ControllerRegistry.setAttendanceController(attendanceController);

            // Pass session to the attendance controller
            attendanceController.setSession(session);

            // Provide a callback so AttendanceController can go back to the sessions list
            attendanceController.setBackHandler(() -> {
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
            showError("Error opening attendance page: " + e.getMessage());
        }
    }

    private void loadSessionsFromDatabase() {
        try {
            List<Session> sessions = ss.getAllSessions();

            // Sort sessions by sessionId in ascending order
            sessions.sort(Comparator.comparing(Session::getSessionId));

            sessionList.setAll(sessions);
            sessionTable.setItems(sessionList);

            // Clear selection map and repopulate
            selectionMap.clear();
            for (Session session : sessions) {
                selectionMap.put(session.getSessionId(), new SimpleBooleanProperty(false));

                // Store the session id if the session loaded is Open
                if ("Open".equals(session.getStatus())) {
                    ApplicationContext.getAuthSession().setActiveSessionId(session.getSessionId());
                }
            }

            showInfo("Loaded " + sessions.size() + " sessions");
            updateButtonStates();
        } catch (Exception e) {
            showError("Error loading sessions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper methods for different message types
    private void showSuccess(String message) {
        styleInfoLabel("success", "✓ " + message);
    }

    private void showError(String message) {
        styleInfoLabel("error", "✗ " + message);
    }

    private void showWarning(String message) {
        styleInfoLabel("warning", "⚠ " + message);
    }

    private void showInfo(String message) {
        styleInfoLabel("normal", "ℹ " + message);
    }

    // Update button states
    private void updateButtonStates() {
        List<Session> selectedSessions = getSelectedSessions();
        boolean hasSelection = !selectedSessions.isEmpty();

        // Update delete button state
        if (deleteButton != null) {
            if (!hasSelection) {
                deleteButton.setDisable(true);
            } else if (selectedSessions.stream().anyMatch(s -> "Open".equals(s.getStatus()))) {
                deleteButton.setDisable(true);
            } else {
                deleteButton.setDisable(false);
            }
        }

        // Update start button state - only enabled when exactly one session is selected
        // and session status is 'Pending'
        if (startButton != null) {
            if (!hasSelection) {
                startButton.setDisable(true);
            } else if (selectedSessions.size() > 1) {
                startButton.setDisable(true);
            } else if (selectedSessions.stream().anyMatch(s -> !"Pending".equals(s.getStatus()))) {
                startButton.setDisable(true);
            } else {
                startButton.setDisable(false);
            }
        }

        // Update stop button state - only enabled if sessions selected are not 'Open'
        if (stopButton != null) {
            if (!hasSelection) {
                stopButton.setDisable(true);
            } else if (selectedSessions.stream().anyMatch(s -> !"Open".equals(s.getStatus()))) {
                stopButton.setDisable(true);
            } else {
                stopButton.setDisable(false);
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
                showSuccess("Session " + newSession.getSessionId() + " created successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error creating session: " + e.getMessage());
        }
    }

    @FXML
    private void onStartSession() {
        // Selection of sessions to start
        List<Session> selectedSessions = getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            showWarning("Please select session(s) to start.");
            return;
        }

        // Check if more than one session is selected
        if (selectedSessions.size() > 1) {
            showError("Cannot open multiple sessions at once. Please select only one session to open.");
            return;
        }

        // Check if there's already an open session
        if (ss.isSessionOpen()) {
            showError(
                    "There is already an open session. Please close the current open session before starting a new one.");
            return;
        }

        // Get the single selected session
        Session session = selectedSessions.get(0);

        // Validate session status
        if (!"Pending".equals(session.getStatus())) {
            showError("Can only start sessions with 'Pending' status");
            return;
        } else {
            // Start the session
            session.open();
            ss.updateSessionStatus(session);

            ApplicationContext.getAuthSession().setActiveSessionId(session.getSessionId());
        }

        sessionTable.refresh();
        clearAllSelection();
        showSuccess("Session " + session.getSessionId() + " started successfully.");
    }

    @FXML
    private void onStopSession() {
        // Selection of sessions to stop if session status != "Closed"
        List<Session> selectedSessions = getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            showWarning("Please select session(s) to stop.");
            return;
        }

        int successCount = 0;
        for (Session session : selectedSessions) {
            if (!"Closed".equals(session.getStatus())) {
                session.close();
                ss.updateSessionStatus(session);

                // Clear Session ID from AuthSession
                Integer activeSessionId = ApplicationContext.getAuthSession().getActiveSessionId();
                if (activeSessionId != null && activeSessionId == session.getSessionId()) {
                    ApplicationContext.getAuthSession().clearActiveSessionId();
                }

                successCount++;
            }
        }

        sessionTable.refresh();
        clearAllSelection();
        showSuccess("Stopped " + successCount + " session(s) successfully.");
    }

    @FXML
    private void onDeleteSession() {
        // Selection of sessions to delete if session status != "Open"
        List<Session> selectedSessions = getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            showWarning("Please select session(s) to delete.");
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
            alert.setContentText("WARNING: This will permanently delete ALL " + sessionList.size() + " sessions!\n\n"
                    + "This action cannot be undone. Are you absolutely sure?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    ss.deleteAll();
                    loadSessionsFromDatabase();
                    showSuccess("Successfully deleted all sessions.");
                } catch (Exception e) {
                    showError("Error deleting all sessions: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            // Check if any open sessions are selected
            boolean hasOpenSessions = selectedSessions.stream()
                    .anyMatch(s -> "Open".equals(s.getStatus()));

            if (hasOpenSessions) {
                showWarning("Cannot delete open sessions. Stop the sessions first.");
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
                        ss.deleteSession(session.getSessionId());
                        successCount++;
                    }

                    loadSessionsFromDatabase();
                    showSuccess("Successfully deleted " + successCount + " session(s).");
                } catch (Exception e) {
                    showError("Error deleting sessions: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupAutoStartColumn() {
        // System.out.println("SessionController: Setting up Auto Start column");
        colAutoStart.setCellFactory(column -> new TableCell<Session, Boolean>() {
            private final ToggleButton toggleButton = new ToggleButton();
            private boolean initializing = true;

            {
                toggleButton.setPrefWidth(60);
                toggleButton.setPrefHeight(25);

                toggleButton.setOnAction(event -> {
                    if (!initializing) {
                        Session session = getTableView().getItems().get(getIndex());
                        if (session != null) {
                            boolean newAutoStart = toggleButton.isSelected();
                            // System.out.println("Auto Start button CLICKED for session " +
                            // session.getSessionId());

                            // Update the setting (no validation needed - button is disabled if not allowed)
                            session.setAutoStart(newAutoStart);
                            ss.updateAutoSettings(session.getSessionId(), newAutoStart, session.isAutoStop());

                            showSuccess("Auto Start " + (newAutoStart ? "enabled" : "disabled") + " for session "
                                    + session.getSessionId());

                            sessionTable.refresh();
                        }
                    }
                });
            }

            private void updateButtonAppearance() {
                if (toggleButton.isSelected()) {
                    toggleButton.setText("ON");
                    toggleButton
                            .setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                } else {
                    toggleButton.setText("OFF");
                    toggleButton
                            .setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
                }
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Session session = getTableRow().getItem();

                    initializing = true;

                    // Auto-start logic:
                    // - NEVER allowed for Closed sessions
                    // - Only allowed for Pending sessions that haven't ended
                    boolean canHaveAutoStart = ss.canSessionHaveAutoStart(session);

                    // If auto-start is enabled but no longer allowed, disable it
                    if (!canHaveAutoStart && session.isAutoStart()) {
                        // System.out.println(
                        // "SessionController: Auto-start was enabled but is no longer allowed -
                        // disabling");
                        session.setAutoStart(false);
                        ss.updateAutoSettings(session.getSessionId(), false, session.isAutoStop());
                    }

                    toggleButton.setSelected(session.isAutoStart());
                    toggleButton.setDisable(!canHaveAutoStart);

                    updateButtonAppearance();
                    initializing = false;

                    setGraphic(toggleButton);
                }
            }
        });
    }

    private void setupAutoStopColumn() {
        System.out.println("SessionController: Setting up Auto Stop column");
        colAutoStop.setCellFactory(column -> new TableCell<Session, Boolean>() {
            private final ToggleButton toggleButton = new ToggleButton();
            private boolean initializing = true;

            {
                toggleButton.setPrefWidth(60);
                toggleButton.setPrefHeight(25);

                toggleButton.setOnAction(event -> {
                    if (!initializing) {
                        Session session = getTableView().getItems().get(getIndex());
                        if (session != null) {
                            boolean newAutoStop = toggleButton.isSelected();
                            // System.out.println("Auto Stop button CLICKED for session " +
                            // session.getSessionId());

                            // Update the setting
                            session.setAutoStop(newAutoStop);
                            ss.updateAutoSettings(session.getSessionId(), session.isAutoStart(), newAutoStop);

                            showSuccess("Auto Stop " + (newAutoStop ? "enabled" : "disabled") + " for session "
                                    + session.getSessionId());

                            sessionTable.refresh();
                        }
                    }
                });
            }

            private void updateButtonAppearance() {
                if (toggleButton.isSelected()) {
                    toggleButton.setText("ON");
                    toggleButton
                            .setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                } else {
                    toggleButton.setText("OFF");
                    toggleButton
                            .setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
                }
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Session session = getTableRow().getItem();

                    initializing = true;

                    // Auto-stop logic:
                    // - NEVER allowed for Closed sessions
                    // - Allowed for Pending/Open sessions (even if ended - to allow closing)
                    boolean canHaveAutoStop = ss.canSessionHaveAutoStop(session);

                    // If auto-stop is enabled but no longer allowed, disable it
                    if (!canHaveAutoStop && session.isAutoStop()) {
                        // System.out.println(
                        // "SessionController: Auto-stop was enabled but is no longer allowed -
                        // disabling");
                        session.setAutoStop(false);
                        ss.updateAutoSettings(session.getSessionId(), session.isAutoStart(), false);
                    }

                    toggleButton.setSelected(session.isAutoStop());
                    toggleButton.setDisable(!canHaveAutoStop);

                    updateButtonAppearance();
                    initializing = false;

                    setGraphic(toggleButton);
                }
            }
        });
    }
}

// Cannot remove the selection effect?
// implement edit session function?
// ensure view for each type of user (e.g. admin, ta, student, prof) is
// different
// cannot create a session from 23:00 to 00:00?
// - if late threshold is not filled in, use deefault threshold in
// config.properties
// - late threshold cannot be more than duration of session
