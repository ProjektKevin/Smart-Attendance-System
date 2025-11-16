package com.smartattendance.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.smartattendance.model.entity.Session;
import com.smartattendance.service.AttendanceObserver;
import com.smartattendance.service.SessionService;
import com.smartattendance.util.AutoSessionProcessor;
import com.smartattendance.util.ButtonStateManager;
import com.smartattendance.util.DialogUtil;
import com.smartattendance.util.InfoLabelUtil;
import com.smartattendance.util.SessionTableUtil;
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

/**
 * Controller for managing attendance sessions in the Smart Attendance System.
 * Handles session creation, starting, stopping, deletion, and navigation to
 * attendance views.
 * Implements {@link AttendanceObserver} to receive attendance change
 * notifications and update UI accordingly.
 * 
 * This controller manages:
 * - Session table display and selection
 * - Session lifecycle operations (start, stop, delete)
 * - Navigation between session list and attendance views
 * - Automatic session processing
 * - Button state management based on selection and session status
 * 
 * @author Lim Jia Hui
 * @author Chue Wan Yan (modified openAttendancePage, added javadoc comments)
 *
 * @version 18:00 16 Nov 2025
 *
 */
public class SessionController {

    // ========== FXML COMPONENTS ==========

    /** Info label for displaying status messages to the user */
    @FXML
    private Label sessionsInfo;

    /** Table view for displaying session records */
    @FXML
    private TableView<Session> sessionTable;

    /** Table column for session selection checkbox */
    @FXML
    private TableColumn<Session, Boolean> colSelect;

    /** Table column for session ID display */
    @FXML
    private TableColumn<Session, String> colId;

    /** Table column for course code display */
    @FXML
    private TableColumn<Session, String> colCourse;

    /** Table column for session date */
    @FXML
    private TableColumn<Session, LocalDate> colDate;

    /** Table column for session start time */
    @FXML
    private TableColumn<Session, LocalTime> colStart;

    /** Table column for session end time */
    @FXML
    private TableColumn<Session, LocalTime> colEnd;

    /** Table column for session location */
    @FXML
    private TableColumn<Session, String> colLoc;

    /** Table column for late threshold minutes */
    @FXML
    private TableColumn<Session, Integer> colLate;

    /** Table column for session status (Pending/Open/Closed) */
    @FXML
    private TableColumn<Session, String> colStatus;

    /** Table column for auto-start toggle */
    @FXML
    private TableColumn<Session, Boolean> colAutoStart;

    /** Table column for auto-stop toggle */
    @FXML
    private TableColumn<Session, Boolean> colAutoStop;

    /** Table column for view more actions */
    @FXML
    private TableColumn<Session, Void> colViewMore;

    /** Button for deleting selected sessions */
    @FXML
    private Button deleteButton;

    /** Button for starting selected sessions */
    @FXML
    private Button startButton;

    /** Button for stopping selected sessions */
    @FXML
    private Button stopButton;

    /** Checkbox for selecting all sessions in the table */
    @FXML
    private CheckBox selectAllCheckBox;

    /** Container for the session list view */
    @FXML
    private VBox sessionListContainer;

    /** Container for the attendance view (hidden by default) */
    @FXML
    private VBox attendanceViewContainer;

    // ========== SERVICE DEPENDENCIES ==========

    /** Service for session-related business logic and data operations */
    private final SessionService ss = new SessionService();

    /** Util for managing session table operations and data binding */
    private SessionTableUtil st;
    private SessionViewNavigator svn;
    private ButtonStateManager bsm;
    private final DialogUtil du = new DialogUtil();
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

        this.st = new SessionTableUtil(
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

        // Set up table columns and load initial data
        st.setupTableColumns();
        st.loadSessions();

        // Initialise button state manager
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

        // Initialise view navigator for session-attendance view transitions
        this.svn = new SessionViewNavigator(sessionListContainer, attendanceViewContainer);

        // Start automatic session processor (runs every 30 seconds)
        AutoSessionProcessor autoProcessor = new AutoSessionProcessor(ss, st);
        autoProcessor.start();
    }

    // ========== MESSAGE DISPLAY METHODS USING INFOLABELUTIL ==========

    /**
     * Displays a success message to the user.
     *
     * @param message The success message to display
     */
    private void showSuccess(String message) {
        InfoLabelUtil.showSuccess(sessionsInfo, message);
    }

    /**
     * Displays an error message to the user.
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        InfoLabelUtil.showError(sessionsInfo, message);
    }

    /**
     * Displays a warning message to the user.
     *
     * @param message The warning message to display
     */
    private void showWarning(String message) {
        InfoLabelUtil.showWarning(sessionsInfo, message);
    }

    /**
     * Displays an informational message to the user.
     *
     * @param message The informational message to display
     */
    private void showInfo(String message) {
        InfoLabelUtil.showInfo(sessionsInfo, message);
    }

    // ========== NAVIGATION METHODS ==========
    /**
     * Opens the attendance page (attendance records for each student) for the
     * specified session.
     * Loads the attendance view FXML and initialises the attendance controller
     * with the selected session data.
     *
     * @param session The session for which to display attendance records
     * @throws IOException if the attendance view FXML cannot be loaded
     */
    private void openAttendancePage(Session session) {
        try {
            svn.openAttendanceView("/view/AttendanceView.fxml", session);
            attendanceController = svn.getAttendanceController();
        } catch (IOException e) {
            // Display user-friendly message
            showError("Error opening attendance page: " + e.getMessage());
        }
    }

    // ========== EVENT HANDLER METHODS ==========

    /**
     * Handles the select all checkbox action.
     * Selects or deselects all sessions in the table based on checkbox state
     * and updates button states accordingly.
     */
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

    /**
     * Handles the create session action.
     * Opens a modal dialog (SessionForm.fxml) for creating a new session and adds
     * the session
     * to the table upon successful creation.
     */
    @FXML
    private void onCreateSession() {
        try {
            // Load the form dialog
            FXMLLoader loader = svn.openModalDialog("/view/SessionForm.fxml", "Create New Session");
            SessionFormController formCtrl = loader.getController();
            Session newSession = formCtrl.getNewSession(); // get the newly created session

            if (newSession != null) {
                st.loadSessions(); // Reload to get the new session
                showSuccess("Session " + newSession.getSessionId() + " created successfully!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error creating session: " + e.getMessage());
        }
    }

    /**
     * Handles the start session action.
     * Validates and starts the selected session if conditions are met.
     * Only one session can be started at a time.
     */
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
            // After validation, attempt to start the session
            ss.startSessionIfValid(session);
            st.refreshTable();
            st.clearAllSelection();
            showSuccess("Session " + session.getSessionId() + " started successfully.");
        } catch (IllegalStateException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Handles the stop session action.
     * Stops all selected sessions that are currently pending or open for stopping.
     *
     * @return The number of sessions successfully stopped
     */
    @FXML
    private void onStopSession() {
        // Selection of sessions to stop if session status != "Closed"
        List<Session> selectedSessions = st.getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            showWarning("Please select session(s) to stop.");
            return;
        }

        // After validation, attempt to stop the session
        int successCount = ss.stopSessionsIfValid(selectedSessions);
        st.refreshTable();
        st.clearAllSelection();
        showSuccess("Stopped " + successCount + " session(s) successfully.");
    }

    /**
     * Handles the delete session action.
     * Deletes selected sessions after confirmation, with special handling
     * for "delete all" scenario and validation for open sessions.
     */
    @FXML
    private void onDeleteSession() {
        // Selection of sessions to delete if session status != "Open"
        List<Session> selectedSessions = st.getSelectedSessions();
        if (selectedSessions.isEmpty()) {
            showWarning("Please select session(s) to delete.");
            return;
        }

        // Check if all sessions are selected and none are open
        boolean shouldDeleteAll = selectedSessions.size() == sessionList.size()
                && selectedSessions.stream().noneMatch(s -> "Open".equals(s.getStatus()));

        /**
         * Handles deletion of all sessions after user confirmation.
         * Displays a warning dialog due to the destructive nature of this operation.
         */
        if (shouldDeleteAll) {
            // Use deleteAll when all sessions are selected
            boolean confirmed = du.showConfirmation(
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
            /**
             * Handles deletion of selected sessions after validation and confirmation.
             */
            // Check if any open sessions are selected (cannot delete open sessions)
            boolean hasOpenSessions = selectedSessions.stream().anyMatch(s -> "Open".equals(s.getStatus()));
            if (hasOpenSessions) {
                showWarning("Cannot delete open sessions. Stop the sessions first.");
                return;
            }

            // Create comma-separated list of session IDs for confirmation message
            String sessionIds = selectedSessions.stream()
                    .map(Session::getSessionId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            // Show confirmation dialog - confirm deletion for selected sessions
            boolean confirmed = du.showConfirmation(
                    "Delete " + selectedSessions.size() + " Session(s)",
                    "Delete " + selectedSessions.size() + " Session(s)",
                    "Are you sure you want to delete the selected session(s)?\n" + sessionIds);

            // Use deleteSession when some sessions are selected
            if (confirmed) {
                try {
                    // Delete each selected session
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