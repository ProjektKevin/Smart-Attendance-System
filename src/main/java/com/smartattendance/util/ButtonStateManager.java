package com.smartattendance.util;

import com.smartattendance.model.entity.Session;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import java.util.*;

/**
 * Manages the state and behavior of action buttons based on session selection
 * and status.
 * 
 * This utility class coordinates the enabled/disabled states of UI buttons
 * according
 * to business rules and current selection context. It ensures that users can
 * only
 * perform actions that are valid for the currently selected sessions.
 * 
 * Button State Rules:
 * - Delete Button: Enabled when sessions are selected AND no open sessions are
 * selected
 * - Start Button: Enabled when exactly ONE pending session is selected
 * - Stop Button: Enabled when one or more pending or open sessions are selected
 * - Select All Checkbox: Checked when all sessions in the list are selected
 * 
 * This class follows the State Pattern to manage complex UI state transitions
 * and ensures consistent user experience throughout the application.
 * 
 * @author Lim Jia Hui
 * @version 20:57 16 Nov 2025
 */
public class ButtonStateManager {

    // ========== UI COMPONENT REFERENCES ==========

    /** Button for deleting selected sessions */
    private final Button deleteButton;

    /** Button for starting selected sessions */
    private final Button startButton;

    /** Button for stopping selected sessions */
    private final Button stopButton;

    /** Checkbox for selecting/deselecting all sessions */
    private final CheckBox selectAllCheckBox;

    // ========== BUSINESS LOGIC DEPENDENCIES ==========

    /** Utility for session table operations and selection management */
    private final SessionTableUtil st;

    /** Observable list of all sessions for selection state tracking */
    private final ObservableList<Session> sessionList;

    // ========== CONSTRUCTOR ==========

    /**
     * Constructs a new ButtonStateManager with all required UI components and
     * dependencies.
     *
     * @param deleteButton      the button for deleting sessions, may be null
     * @param startButton       the button for starting sessions, may be null
     * @param stopButton        the button for stopping sessions, may be null
     * @param selectAllCheckBox the checkbox for selecting all sessions, may be null
     * @param sessionTableUtil  the utility for session table operations, must not
     *                          be null
     * @param sessionList       the observable list of sessions, must not be null
     * @throws IllegalArgumentException if sessionTableUtil or sessionList is null
     */
    public ButtonStateManager(Button deleteButton, Button startButton, Button stopButton,
            CheckBox selectAllCheckBox, SessionTableUtil st,
            ObservableList<Session> sessionList) {
        this.deleteButton = deleteButton;
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.selectAllCheckBox = selectAllCheckBox;
        this.st = st;
        this.sessionList = sessionList;
    }

    /**
     * Updates the enabled/disabled state of all managed buttons based on current
     * selection.
     * 
     * This method evaluates the current session selection against business rules
     * and updates each button's state accordingly. It should be called whenever:
     * - Session selection changes
     * - Session status changes
     * - Session list is updated
     * - UI needs to refresh button states
     * 
     * Business Rules Applied:
     * Delete Button:
     * - Enabled: At least one session selected AND none of them are "Open"
     * - Disabled: No selection OR any selected session is "Open"
     *
     * Start Button:
     * - Enabled: Exactly one session selected AND its status is "Pending"
     * - Disabled: No selection, multiple selections, or selected session is not
     * "Pending"
     *
     * Stop Button:
     * - Enabled: One or more selected sessions are "Open"
     * - Disabled: No selection OR no selected sessions are "Open"
     *
     * Select-All Checkbox:
     * - Checked: All sessions are selected
     * - Unchecked: Not all sessions are selected
     * 
     */
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