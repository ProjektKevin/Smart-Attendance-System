package com.smartattendance.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.smartattendance.model.entity.Session;
import com.smartattendance.service.SessionService;
import com.smartattendance.util.AutoSessionProcessor;
import com.smartattendance.util.ButtonStateManager;
import com.smartattendance.util.ControllerRegistry;
import com.smartattendance.util.DialogService;
import com.smartattendance.util.InfoLabelUtil;
import com.smartattendance.util.SessionTableService;
import com.smartattendance.util.SessionViewNavigator;
import com.smartattendance.util.security.log.ApplicationLogger;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

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
    private SessionTableService st;
    private SessionViewNavigator svn;
    private ButtonStateManager bsm;
    private final DialogService ds = new DialogService();
    private final ObservableList<Session> sessionList = FXCollections.observableArrayList();
    private final Map<Integer, SimpleBooleanProperty> selectionMap = new HashMap<>();
    // F_MA: added by felicia handling marking attendance
    private AttendanceController attendanceController; // store reference

    // Logger
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    @FXML
    public void initialize() {

        attendanceViewContainer.setVisible(false);
        attendanceViewContainer.setManaged(false);

        this.st = new SessionTableService(
                sessionTable, selectionMap, sessionList, ss,
                colSelect, colId, colCourse, colDate, colStart, colEnd,
                colLoc, colLate, colStatus, colAutoStart, colAutoStop, colViewMore,
                this::openAttendancePage, // Method reference for view more
                () -> {
                    if (bsm != null)
                        bsm.updateButtonStates();
                }, // Method reference for selection change
                this::showInfo, // For loadSessions()
                this::showSuccess // for auto start/stop toggles
        );

        st.setupTableColumns();

        st.loadSessions();

        bsm = new ButtonStateManager(deleteButton, startButton, stopButton, selectAllCheckBox, st, sessionList);
        bsm.updateButtonStates();

        // Add listener to update button states when selection changes
        sessionTable.itemsProperty().addListener((obs, oldItems, newItems) -> bsm.updateButtonStates());

        // Select All checkbox listener
        if (selectAllCheckBox != null) {
            selectAllCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    st.selectAllSessions();
                } else {
                    st.clearAllSelection();
                }
                bsm.updateButtonStates();
            });
        }

        this.svn = new SessionViewNavigator(sessionListContainer, attendanceViewContainer);

        // Auto session processor - runs every 30 seconds
        AutoSessionProcessor autoProcessor = new AutoSessionProcessor(ss, st);
        autoProcessor.start();
    }

    public AttendanceController getAttendanceController() {
        return attendanceController;
    }

    // ========== MESSAGE METHODS USING INFOLABELUTIL ==========

    private void showSuccess(String message) {
        InfoLabelUtil.showSuccess(sessionsInfo, message);
    }

    private void showError(String message) {
        InfoLabelUtil.showError(sessionsInfo, message);
    }

    private void showWarning(String message) {
        InfoLabelUtil.showWarning(sessionsInfo, message);
    }

    private void showInfo(String message) {
        InfoLabelUtil.showInfo(sessionsInfo, message);
    }

    // ========== NAVIGATION ==========
    private void openAttendancePage(Session session) {
        try {
            svn.openAttendanceView("/view/AttendanceView.fxml", session);
            attendanceController = svn.getAttendanceController();
            ControllerRegistry.setAttendanceController(attendanceController);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error opening attendance page: " + e.getMessage());
        }
    }

    // ========== EVENT HANDLERS ==========

    @FXML
    private void onSelectAll() {
        // When select all checkbox is selected
        if (selectAllCheckBox.isSelected()) {
            st.selectAllSessions();
        } else {
            st.clearAllSelection();
        }
        bsm.updateButtonStates();
    }

    @FXML
    private void onCreateSession() {
        try {
            FXMLLoader loader = svn.openModalDialog("/view/SessionForm.fxml", "Create New Session");
            SessionFormController formCtrl = loader.getController();
            Session newSession = formCtrl.getNewSession();

            if (newSession != null) {
                st.loadSessions();
                showSuccess("Session " + newSession.getSessionId() + " created successfully!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error creating session: " + e.getMessage());
        }
    }

    @FXML
    private void onStartSession() {
        // Selection of sessions to start
        List<Session> selectedSessions = st.getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            showWarning("Please select session(s) to start.");
            return;
        }

        // Check if more than one session is selected
        if (selectedSessions.size() > 1) {
            showError("Cannot open multiple sessions at once. Please select only one session to open.");
            return;
        }

        // Get the single selected session
        Session session = selectedSessions.get(0);

        // Validate session status & check if there is already an open session
        try {
            ss.startSessionIfValid(session); 
            st.refreshTable();
            st.clearAllSelection();
            showSuccess("Session " + session.getSessionId() + " started successfully.");
        } catch (IllegalStateException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onStopSession() {
        // Selection of sessions to stop if session status != "Closed"
        List<Session> selectedSessions = st.getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            showWarning("Please select session(s) to stop.");
            return;
        }

        int successCount = ss.stopSessionsIfValid(selectedSessions); 
        st.refreshTable();
        st.clearAllSelection();
        showSuccess("Stopped " + successCount + " session(s) successfully.");
    }

    @FXML
    private void onDeleteSession() {
        // Selection of sessions to delete if session status != "Open"
        List<Session> selectedSessions = st.getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            showWarning("Please select session(s) to delete.");
            return;
        }

        // Check if all sessions are selected and none are open
        boolean shouldDeleteAll = selectedSessions.size() == sessionList.size() &&
                selectedSessions.stream().noneMatch(s -> "Open".equals(s.getStatus()));

        if (shouldDeleteAll) {
            boolean confirmed = ds.showConfirmation(
                    "Confirm Delete All",
                    "Delete ALL Sessions",
                    "WARNING: This will permanently delete ALL " + sessionList.size() + " sessions!\n\n"
                            + "This action cannot be undone. Are you absolutely sure?");

            if (confirmed) {
                try {
                    ss.deleteAll();
                    st.loadSessions();
                    showSuccess("Successfully deleted all sessions.");
                } catch (Exception e) {
                    showError("Error deleting all sessions: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            // Check for any open sessions
            boolean hasOpenSessions = selectedSessions.stream().anyMatch(s -> "Open".equals(s.getStatus()));
            if (hasOpenSessions) {
                showWarning("Cannot delete open sessions. Stop the sessions first.");
                return;
            }

            // Confirm deletion for selected sessions
            String sessionIds = selectedSessions.stream()
                    .map(Session::getSessionId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            boolean confirmed = ds.showConfirmation(
                    "Delete " + selectedSessions.size() + " Session(s)",
                    "Delete " + selectedSessions.size() + " Session(s)",
                    "Are you sure you want to delete the selected session(s)?\n" + sessionIds);

            if (confirmed) {
                try {
                    for (Session session : selectedSessions) {
                        ss.deleteSession(session.getSessionId());
                    }
                    st.loadSessions();
                    showSuccess("Successfully deleted " + selectedSessions.size() + " session(s).");
                } catch (Exception e) {
                    showError("Error deleting sessions: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}