package com.smartattendance.util;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.util.Callback;

/**
 * A reusable TableCell that displays a CheckBox for boolean-like values in a
 * JavaFX TableView.
 *
 * This cell supports:
 * - Getting the checkbox state using a row-index callback
 * - Handling user interaction through an optional action callback
 * - Correct TableCell lifecycle handling via updateItem
 * - A type-safe generic design for any table data model
 *
 * The checkbox state is refreshed dynamically based on the row index, and the
 * implementation correctly manages state during cell reuse.
 *
 * @param <S> the type of the TableView row items
 * @param <T> the type of the cell value (typically Boolean)
 *
 * @author Lim Jia Hui
 * @version 21:00 16 Nov 2025
 */
public class CheckBoxTableCell<S, T> extends TableCell<S, T> {

    // ========== UI COMPONENTS ==========

    /** The CheckBox component displayed in the table cell */
    private final CheckBox checkBox;

    // ========== CALLBACK INTERFACES ==========

    /**
     * Callback for retrieving the current selection state of a row.
     * Called whenever the cell needs to update its visual state.
     */
    private final Callback<Integer, Boolean> selectedStateCallback;

    /**
     * Callback for handling checkbox state changes.
     * Called when the user interacts with the checkbox.
     */
    private final Callback<Integer, Void> onActionCallback;

    // ========== CONSTRUCTORS ==========

    /**
     * Creates a CheckBoxTableCell with the given state-retrieval and action
     * callbacks.
     *
     * @param selectedStateCallback the callback that returns the checkbox state
     *                              for a given row index; must not be null
     * @param onActionCallback      the callback invoked when the user toggles
     *                              the checkbox; may be null
     *
     * @throws IllegalArgumentException if selectedStateCallback is null
     *
     */
    public CheckBoxTableCell(Callback<Integer, Boolean> selectedStateCallback,
            Callback<Integer, Void> onActionCallback) {
        this.selectedStateCallback = selectedStateCallback;
        this.onActionCallback = onActionCallback;
        this.checkBox = new CheckBox();

        this.checkBox.setOnAction(event -> {
            if (onActionCallback != null && getIndex() >= 0) {
                onActionCallback.call(getIndex());
            }
        });
    }

    // ========== TABLE CELL LIFECYCLE METHODS ==========

    /**
     * Updates the cell based on the current item and whether the cell is empty.
     *
     * Responsibilities:
     * - Show or hide the checkbox depending on the cell's state
     * - Set the checkbox's selected value using the row-index callback
     * - Ensure correct state handling when cells are recycled
     *
     * Always calls super.updateItem to maintain correct TableCell behavior.
     *
     * @param item  the new value for the cell, may be null
     * @param empty true if the cell has no valid row data
     */
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || getIndex() < 0) {
            setGraphic(null);
        } else {
            checkBox.setSelected(selectedStateCallback.call(getIndex()));
            setGraphic(checkBox);
        }
    }
}