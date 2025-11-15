package com.smartattendance.util;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.Session;
import com.smartattendance.service.SessionService;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SessionTableService {
    private final TableView<Session> sessionTable;
    private final Map<Integer, SimpleBooleanProperty> selectionMap;
    private final ObservableList<Session> sessionList;
    private final SessionService sessionService;
    private final Consumer<Session> onViewMoreClick;
    private final Runnable onSelectionChange;
    private final Consumer<String> showInfoMessage;
    private final Consumer<String> showSuccessMessage;

    // Table columns - pass these from controller
    private final TableColumn<Session, Boolean> colSelect;
    private final TableColumn<Session, String> colId;
    private final TableColumn<Session, String> colCourse;
    private final TableColumn<Session, java.time.LocalDate> colDate;
    private final TableColumn<Session, java.time.LocalTime> colStart;
    private final TableColumn<Session, java.time.LocalTime> colEnd;
    private final TableColumn<Session, String> colLoc;
    private final TableColumn<Session, Integer> colLate;
    private final TableColumn<Session, String> colStatus;
    private final TableColumn<Session, Boolean> colAutoStart;
    private final TableColumn<Session, Boolean> colAutoStop;
    private final TableColumn<Session, Void> colViewMore;

    private final SessionService ss = new SessionService();

    public SessionTableService(TableView<Session> sessionTable,
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

    public void setupTableColumns() {
        setupBasicColumns();
        setupCheckBoxColumn();
        setupAutoStartColumn();
        setupAutoStopColumn();
        setUpViewMoreButton();
    }

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
                        onSelectionChange.run(); // Callback to controller
                    }
                    return null;
                }));

        colSelect.setCellValueFactory(cellData -> null);
    }

    private void setUpViewMoreButton() {
        colViewMore.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View More");

            {
                btn.setOnAction(event -> {
                    Session session = getTableView().getItems().get(getIndex());
                    onViewMoreClick.accept(session); // Callback to controller
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

    private void setupAutoStartColumn() {
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

                    boolean canHaveAutoStart = sessionService.canSessionHaveAutoStart(session);
                    if (!canHaveAutoStart && session.isAutoStart()) {
                        session.setAutoStart(false);
                        sessionService.updateAutoSettings(session.getSessionId(), false, session.isAutoStop());
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

                    boolean canHaveAutoStop = sessionService.canSessionHaveAutoStop(session);
                    if (!canHaveAutoStop && session.isAutoStop()) {
                        session.setAutoStop(false);
                        sessionService.updateAutoSettings(session.getSessionId(), session.isAutoStart(), false);
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

    // Public methods for controller to use

    // ========== DATA MANAGEMENT ==========
    /** Load sessions into table and reset selection map */
    public void loadSessions() {
        List<Session> sessions = ss.getAllSessions();
        sessions.sort((a, b) -> Integer.compare(a.getSessionId(), b.getSessionId()));

        sessionList.setAll(sessions);
        sessionTable.setItems(sessionList);

        selectionMap.clear();
        for (Session session : sessions) {
            selectionMap.put(session.getSessionId(), new SimpleBooleanProperty(false));

            // Store the session id if the session loaded is Open
            if ("Open".equals(session.getStatus())) {
                ApplicationContext.getAuthSession().setActiveSessionId(session.getSessionId());
            }
        }

        showInfo("Loaded " + sessions.size() + " sessions");
    }

    public List<Session> getSelectedSessions() {
        return sessionList.stream()
                .filter(session -> selectionMap.get(session.getSessionId()).get())
                .collect(Collectors.toList());
    }

    public void selectAllSessions() {
        selectionMap.values().forEach(prop -> prop.set(true));
        sessionTable.refresh();
    }

    public void clearAllSelection() {
        selectionMap.values().forEach(prop -> prop.set(false));
        sessionTable.refresh();
    }

    public void refreshTable() {
        sessionTable.refresh();
    }

    private void showInfo(String message) {
        if (showInfoMessage != null) {
            showInfoMessage.accept(message);
        }
    }
}