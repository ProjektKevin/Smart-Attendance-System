package com.smartattendance.controller.student;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class StudentAttendanceRowStyler {

    public static void setupDetailColumns(
            TableView<StudentAttendanceRow> table,
            TableColumn<StudentAttendanceRow, java.time.LocalDate> colDate,
            TableColumn<StudentAttendanceRow, String> colModule,
            TableColumn<StudentAttendanceRow, String> colStatus,
            TableColumn<StudentAttendanceRow, String> colMethod,
            TableColumn<StudentAttendanceRow, String> colMarkedAt
    ) {
        if (colDate != null)   colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (colModule != null) colModule.setCellValueFactory(new PropertyValueFactory<>("module"));

        if (colStatus != null) {
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colStatus.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                        return;
                    }
                    setText(item);
                    String lower = item.toLowerCase();
                    setStyle("-fx-alignment: CENTER-LEFT;");
                    switch (lower) {
                        case "present" -> setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #2e7d32;");
                        case "late"    -> setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #f57c00;");
                        case "absent"  -> setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #c62828;");
                        case "pending" -> setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #616161;");
                    }
                }
            });
        }

        if (colMethod != null)   colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        if (colMarkedAt != null) colMarkedAt.setCellValueFactory(new PropertyValueFactory<>("markedAt"));

        if (table != null) {
            table.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(StudentAttendanceRow row, boolean empty) {
                    super.updateItem(row, empty);
                    if (empty || row == null) {
                        setStyle("");
                        return;
                    }
                    String status = row.getStatus() != null ? row.getStatus().toLowerCase() : "";
                    switch (status) {
                        case "present" -> setStyle("-fx-background-color: rgba(46,125,50,0.10);");
                        case "late"    -> setStyle("-fx-background-color: rgba(245,124,0,0.10);");
                        case "absent"  -> setStyle("-fx-background-color: rgba(198,40,40,0.10);");
                        case "pending" -> setStyle("-fx-background-color: rgba(97,97,97,0.06);");
                        default        -> setStyle("");
                    }
                }
            });
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
    }
}
