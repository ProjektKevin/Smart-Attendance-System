// CheckBoxTableCell.java
package com.smartattendance.util;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class CheckBoxTableCell<S, T> extends TableCell<S, T> {
    private final CheckBox checkBox;
    private final Callback<Integer, Boolean> selectedStateCallback;
    private final Callback<Integer, Void> onActionCallback;

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