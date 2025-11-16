package com.smartattendance.util;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.Session;
import com.smartattendance.service.SessionService;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Utility class for managing and configuring the Session TableView in the Smart
 * Attendance System.
 * 
 * This class encapsulates all table-related functionality including column
 * setup,
 * cell rendering, selection management, and interaction handling. It serves as
 * a
 * bridge between the controller and the TableView, providing a clean separation
 * of concerns.
 * 
 * Key responsibilities:
 * - Table column configuration and cell factory setup
 * - Selection state management using a selection map
 * - Interactive controls (checkboxes, toggle buttons, action buttons)
 * - Data loading and refresh operations
 * - Callback handling for user interactions
 * 
 * This class follows the Facade pattern to simplify complex TableView
 * operations
 * and provides a consistent interface for session table management.
 * 
 * @author Lim Jia Hui 
 * @version 21:23 16 Nov 2025
 */
public class SessionTableUtil {

    // ========== TABLE COMPONENTS ==========

    /** The main TableView component displaying session data */
    private final TableView<Session> sessionTable;

    /** Map tracking selection state of sessions by session ID */
    private final Map<Integer, SimpleBooleanProperty> selectionMap;

    /** Observable list containing the session data for the table */
    private final ObservableList<Session> sessionList;

    /** Service for session-related business operations */
    private final SessionService sessionService;

    // ========== CALLBACK INTERFACES ==========

    /** Callback for handling view more button clicks */
    private final Consumer<Session> onViewMoreClick;

    /** Callback for handling selection changes */
    private final Runnable onSelectionChange;

    /** Callback for displaying informational messages */
    private final Consumer<String> showInfoMessage;

    /** Callback for displaying success messages */
    private final Consumer<String> showSuccessMessage;

    // ========== TABLE COLUMNS ==========

    /** Column for session selection checkboxes */
    private final TableColumn<Session, Boolean> colSelect;

    /** Column for session ID display */
    private final TableColumn<Session, String> colId;

    /** Column for course code */
    private final TableColumn<Session, String> colCourse;

    /** Column for session date */
    private final TableColumn<Session, java.time.LocalDate> colDate;

    /** Column for session start time */
    private final TableColumn<Session, java.time.LocalTime> colStart;

    /** Column for session end time */
    private final TableColumn<Session, java.time.LocalTime> colEnd;

    /** Column for session location */
    private final TableColumn<Session, String> colLoc;

    /** Column for late threshold in minutes */
    private final TableColumn<Session, Integer> colLate;

    /** Column for session status (Pending/Open/Closed) */
    private final TableColumn<Session, String> colStatus;

    /** Column for auto-start toggle buttons */
    private final TableColumn<Session, Boolean> colAutoStart;

    /** Column for auto-stop toggle buttons */
    private final TableColumn<Session, Boolean> colAutoStop;

    /** Column for view more action buttons */
    private final TableColumn<Session, Void> colViewMore;

    // ========== SERVICE DEPENDENCIES ==========

    /** Service instance for data operations */
    private final SessionService ss = new SessionService();

    // ========== CONSTRUCTOR ==========

    /**
     * Constructs a new SessionTableUtil with all required dependencies and
     * callbacks.
     *
     * @param sessionTable       the TableView component to manage, must not be null
     * @param selectionMap       the map for tracking session selection states, must
     *                           not be null
     * @param sessionList        the observable list containing session data, must
     *                           not be null
     * @param sessionService     the service for session operations, must not be
     *                           null
     * @param colSelect          the selection checkbox column
     * @param colId              the session ID column
     * @param colCourse          the course column
     * @param colDate            the date column
     * @param colStart           the start time column
     * @param colEnd             the end time column
     * @param colLoc             the location column
     * @param colLate            the late threshold column
     * @param colStatus          the status column
     * @param colAutoStart       the auto-start toggle column
     * @param colAutoStop        the auto-stop toggle column
     * @param colViewMore        the view more action column
     * @param onViewMoreClick    callback for view more button clicks, must not be
     *                           null
     * @param onSelectionChange  callback for selection changes, must not be null
     * @param showInfoMessage    callback for info messages, may be null
     * @param showSuccessMessage callback for success messages, may be null
     * @throws IllegalArgumentException if any required parameter is null
     */
    public SessionTableUtil(TableView<Session> sessionTable,
            Map<Integer, SimpleBooleanProperty> selectionMap,
            ObservableList<Session> sessionList,
            SessionService sessionService,
            TableColumn<Session, Boolean> colSelect,
            TableColumn<Session, String> colId,
            TableColumn<Session, String> colCourse,
            TableColumn<Session, java.time.LocalDate> colDate,
            TableColumn<Session, java.time.LocalTime> colStart,
            TableColumn<Session, java.time.LocalTime> colEnd,
            TableColumn<Session, String> colLoc,
            TableColumn<Session, Integer> colLate,
            TableColumn<Session, String> colStatus,
            TableColumn<Session, Boolean> colAutoStart,
            TableColumn<Session, Boolean> colAutoStop,
            TableColumn<Session, Void> colViewMore,
            Consumer<Session> onViewMoreClick,
            Runnable onSelectionChange,
            Consumer<String> showInfoMessage,
            Consumer<String> showSuccessMessage) {
        this.sessionTable = sessionTable;
        this.selectionMap = selectionMap;
        this.sessionList = sessionList;
        this.sessionService = sessionService;
        this.colSelect = colSelect;
        this.colId = colId;
        this.colCourse = colCourse;
        this.colDate = colDate;
        this.colStart = colStart;
        this.colEnd = colEnd;
        this.colLoc = colLoc;
        this.colLate = colLate;
        this.colStatus = colStatus;
        this.colAutoStart = colAutoStart;
        this.colAutoStop = colAutoStop;
        this.colViewMore = colViewMore;
        this.onViewMoreClick = onViewMoreClick;
        this.onSelectionChange = onSelectionChange;
        this.showInfoMessage = showInfoMessage;
        this.showSuccessMessage = showSuccessMessage;
    }

    // ========== TABLE SETUP METHODS ==========

    /**
     * Configures all table columns with appropriate cell factories and value factories.
     * 
     * This method sets up the complete table structure including:
     *   - Basic data columns using PropertyValueFactory
     *   - Checkbox column for session selection
     *   - Toggle button columns for auto-start/stop settings
     *   - Action button column for view more functionality
     * 
     * This method should be called once during table initialization.
     */
    public void setupTableColumns() {
        setupBasicColumns();
        setupCheckBoxColumn();
        setupAutoStartColumn();
        setupAutoStopColumn();
        setUpViewMoreButton();
    }

    /**
     * Sets up basic data columns using PropertyValueFactory for simple data binding.
     */
    private void setupBasicColumns() {
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
    }

     /**
     * Configures the checkbox column for session selection.
     * 
     * This column uses a custom CheckBoxTableCell that:
     *   - Maintains selection state in the selection map
     *   - Notifies of selection changes via callback
     *   - Handles cell recycling properly
     */
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
                        onSelectionChange.run(); // Notify controller of selection change
                    }
                    return null;
                }));

        colSelect.setCellValueFactory(cellData -> null);
    }

    /**
     * Configures the view more button column with action handling.
     */
    private void setUpViewMoreButton() {
        colViewMore.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View More");

            {
                btn.setOnAction(event -> {
                    Session session = getTableView().getItems().get(getIndex());
                    onViewMoreClick.accept(session); // Delegate to controller
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

    /**
     * Configures the auto-start toggle button column with business rule validation.
     */
    private void setupAutoStartColumn() {
        colAutoStart.setCellFactory(column -> new TableCell<Session, Boolean>() {
            private final ToggleButton toggleButton = new ToggleButton();
            private boolean initializing = true;

            {
                // Configure toggle button appearance
                toggleButton.setPrefWidth(60);
                toggleButton.setPrefHeight(25);

                toggleButton.setOnAction(event -> {
                    if (!initializing) {
                        Session session = getTableView().getItems().get(getIndex());
                        if (session != null) {
                            boolean newAutoStart = toggleButton.isSelected();
                            // Update the setting (no validation needed - button is disabled if not allowed)
                            session.setAutoStart(newAutoStart);
                            sessionService.updateAutoSettings(session.getSessionId(), newAutoStart,
                                    session.isAutoStop());
                            showSuccessMessage
                                    .accept("Auto Start " + (newAutoStart ? "enabled" : "disabled") + " for session "
                                            + session.getSessionId());
                            sessionTable.refresh();
                        }
                    }
                });
            }

            /**
             * Updates the toggle button appearance based on its state.
             */
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

                    // Validate and enforce business rules
                    boolean canHaveAutoStart = sessionService.canSessionHaveAutoStart(session);
                    if (!canHaveAutoStart && session.isAutoStart()) {
                        // Disable auto-start if it violates business rules
                        session.setAutoStart(false);
                        sessionService.updateAutoSettings(session.getSessionId(), false, session.isAutoStop());
                    }

                    // Update button state and appearance
                    toggleButton.setSelected(session.isAutoStart());
                    toggleButton.setDisable(!canHaveAutoStart);
                    updateButtonAppearance();
                    initializing = false;
                    setGraphic(toggleButton);
                }
            }
        });
    }

    /**
     * Configures the auto-stop toggle button column with business rule validation.
     */
    private void setupAutoStopColumn() {
        colAutoStop.setCellFactory(column -> new TableCell<Session, Boolean>() {
            private final ToggleButton toggleButton = new ToggleButton();
            private boolean initializing = true;

            {
                // Configure toggle button appearance
                toggleButton.setPrefWidth(60);
                toggleButton.setPrefHeight(25);

                toggleButton.setOnAction(event -> {
                    if (!initializing) {
                        Session session = getTableView().getItems().get(getIndex());
                        if (session != null) {
                            boolean newAutoStop = toggleButton.isSelected();
                            // Update the setting
                            session.setAutoStop(newAutoStop);
                            sessionService.updateAutoSettings(session.getSessionId(), session.isAutoStart(),
                                    newAutoStop);
                            showSuccessMessage
                                    .accept("Auto Stop " + (newAutoStop ? "enabled" : "disabled") + " for session "
                                            + session.getSessionId());
                            sessionTable.refresh();
                        }
                    }
                });
            }

            /**
             * Updates the toggle button appearance based on its state.
             */
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

                    // Validate and enforce business rules
                    boolean canHaveAutoStop = sessionService.canSessionHaveAutoStop(session);
                    if (!canHaveAutoStop && session.isAutoStop()) {
                        // Disable auto-stop if it violates business rules
                        session.setAutoStop(false);
                        sessionService.updateAutoSettings(session.getSessionId(), session.isAutoStart(), false);
                    }

                    // Update button state and appearance
                    toggleButton.setSelected(session.isAutoStop());
                    toggleButton.setDisable(!canHaveAutoStop);
                    updateButtonAppearance();
                    initializing = false;
                    setGraphic(toggleButton);
                }
            }
        });
    }

    // ========== PUBLIC DATA MANAGEMENT METHODS ==========

    /**
     * Loads sessions into the table and initializes selection states.
     * 
     * This method performs the following operations:
     *   - Retrieves all sessions from the service layer
     *   - Sorts sessions by session ID in ascending order
     *   - Updates the observable list and table items
     *   - Clears and repopulates the selection map
     *   - Updates application context with open sessions
     *   - Displays loading status message
     */
    public void loadSessions() {
        List<Session> sessions = ss.getAllSessions();

        // Sort sessions by sessionId in ascending order
        sessions.sort((a, b) -> Integer.compare(a.getSessionId(), b.getSessionId()));

        // Update table data
        sessionList.setAll(sessions);
        sessionTable.setItems(sessionList);

        // Clear selection map and repopulate
        selectionMap.clear();
        for (Session session : sessions) {
            selectionMap.put(session.getSessionId(), new SimpleBooleanProperty(false));

            // Update application context with open sessions
            if ("Open".equals(session.getStatus())) {
                ApplicationContext.getAuthSession().setActiveSessionId(session.getSessionId());
            }
        }

        showInfo("Loaded " + sessions.size() + " sessions");
    }

    /**
     * Retrieves the list of currently selected sessions.
     *
     * @return a list of selected Session objects, never null
     */
    public List<Session> getSelectedSessions() {
        return sessionList.stream()
                .filter(session -> selectionMap.get(session.getSessionId()).get())
                .collect(Collectors.toList());
    }

    /**
     * Selects all sessions in the table.
     */
    public void selectAllSessions() {
        selectionMap.values().forEach(prop -> prop.set(true));
        sessionTable.refresh();
    }

    /**
     * Clears selection for all sessions in the table.
     */
    public void clearAllSelection() {
        selectionMap.values().forEach(prop -> prop.set(false));
        sessionTable.refresh();
    }

    /**
     * Refreshes the table view to reflect any data changes.
     */
    public void refreshTable() {
        sessionTable.refresh();
    }

    // ========== UTILITY METHODS ==========

    /**
     * Displays an informational message using the provided callback.
     *
     * @param message the message to display
     */
    private void showInfo(String message) {
        if (showInfoMessage != null) {
            showInfoMessage.accept(message);
        }
    }
}