package com.smartattendance.util;

import com.smartattendance.model.entity.Session;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import java.util.*;

public class ButtonStateManager {
    private final Button deleteButton;
    private final Button startButton;
    private final Button stopButton;
    private final CheckBox selectAllCheckBox;
    private final SessionTableService st;
    private final ObservableList<Session> sessionList;

    public ButtonStateManager(Button deleteButton, Button startButton, Button stopButton,
            CheckBox selectAllCheckBox, SessionTableService st,
            ObservableList<Session> sessionList) {
        this.deleteButton = deleteButton;
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.selectAllCheckBox = selectAllCheckBox;
        this.st = st;
        this.sessionList = sessionList;
    }

    public void updateButtonStates() {
        List<Session> selectedSessions = st.getSelectedSessions();
        boolean hasSelection = !selectedSessions.isEmpty();

        // Update delete button state
        if (deleteButton != null) {
            deleteButton
                    .setDisable(!hasSelection || selectedSessions.stream().anyMatch(s -> "Open".equals(s.getStatus())));
        }

        // Update start button state - only enabled when exactly one session is selected
        // and session status is 'Pending'
        if (startButton != null) {
            startButton.setDisable(
                    !hasSelection ||
                            selectedSessions.size() > 1 ||
                            selectedSessions.stream().anyMatch(s -> !"Pending".equals(s.getStatus())));
        }

        // Update stop button state - only enabled if sessions selected are not 'Open'
        if (stopButton != null) {
            stopButton.setDisable(
                    !hasSelection ||
                            selectedSessions.stream().anyMatch(s -> !"Open".equals(s.getStatus())));
        }

        // Update select all checkbox state
        if (selectAllCheckBox != null) {
            selectAllCheckBox.setSelected(hasSelection && selectedSessions.size() == sessionList.size());
        }
    }
}

// // ========== SELECTION MANAGEMENT ==========

// // Update button states
// private void updateButtonStates() {
// List<Session> selectedSessions = st.getSelectedSessions();
// boolean hasSelection = !selectedSessions.isEmpty();

// // Update delete button state
// if (deleteButton != null) {
// if (!hasSelection) {
// deleteButton.setDisable(true);
// } else if (selectedSessions.stream().anyMatch(s ->
// "Open".equals(s.getStatus()))) {
// deleteButton.setDisable(true);
// } else {
// deleteButton.setDisable(false);
// }
// }

// // Update start button state - only enabled when exactly one session is
// selected
// // and session status is 'Pending'
// if (startButton != null) {
// if (!hasSelection) {
// startButton.setDisable(true);
// } else if (selectedSessions.size() > 1) {
// startButton.setDisable(true);
// } else if (selectedSessions.stream().anyMatch(s ->
// !"Pending".equals(s.getStatus()))) {
// startButton.setDisable(true);
// } else {
// startButton.setDisable(false);
// }
// }

// // Update stop button state - only enabled if sessions selected are not
// 'Open'
// if (stopButton != null) {
// if (!hasSelection) {
// stopButton.setDisable(true);
// } else if (selectedSessions.stream().anyMatch(s ->
// !"Open".equals(s.getStatus()))) {
// stopButton.setDisable(true);
// } else {
// stopButton.setDisable(false);
// }
// }

// // Update select all checkbox state
// if (selectAllCheckBox != null) {
// selectAllCheckBox.setSelected(hasSelection && selectedSessions.size() ==
// sessionList.size());
// }
// }