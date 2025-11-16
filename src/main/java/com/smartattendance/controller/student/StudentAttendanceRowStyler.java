package com.smartattendance.controller.student;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Utility class to configure and style the student attendance detail table.
 *
 * <p>
 * This helper centralises the setup for:
 * <ul>
 *     <li>Column value factories for {@link StudentAttendanceRow} fields</li>
 *     <li>Custom cell rendering for the status column (coloured text)</li>
 *     <li>Row-level background colouring based on attendance status</li>
 *     <li>Column resize policy for the detail {@link TableView}</li>
 * </ul>
 * By keeping the styling logic here, controllers can remain focused on
 * loading data and handling user interaction.
 * </p>
 *
 * @author Ernest Lun
 */
public class StudentAttendanceRowStyler {

    /**
     * Configures the detail table columns and row styling for the student
     * attendance table.
     *
     * <p>
     * This method:
     * <ul>
     *     <li>Sets {@link PropertyValueFactory} for each column so that
     *         values are read from {@link StudentAttendanceRow} via getters</li>
     *     <li>Applies a custom {@link TableCell} to the status column to
     *         color the text for different states (Present, Late, Absent, Pending)</li>
     *     <li>Applies row-level background colors based on the row's status</li>
     *     <li>Enables constrained resize policy on the table so columns
     *         stretch to fill the available width</li>
     * </ul>
     * Null checks are performed for all parameters so callers can pass in only
     * the columns they actually use.
     * </p>
     *
     * @param table      the {@link TableView} showing {@link StudentAttendanceRow} items
     * @param colDate    column bound to {@code getDate()}
     * @param colDate    column bound to {@code getDate()}
     * @param colCourse  column bound to {@code getCourse()}
     * @param colStatus  column bound to {@code getStatus()} and styled by status
     * @param colMethod  column bound to {@code getMethod()}
     * @param colMarkedAt column bound to {@code getMarkedAt()}
     */
    public static void setupDetailColumns(
            TableView<StudentAttendanceRow> table,
            TableColumn<StudentAttendanceRow, java.time.LocalDate> colDate,
            TableColumn<StudentAttendanceRow, String> colCourse,
            TableColumn<StudentAttendanceRow, String> colStatus,
            TableColumn<StudentAttendanceRow, String> colMethod,
            TableColumn<StudentAttendanceRow, String> colMarkedAt
    ) {
        // Simple property bindings for date and course.
        if (colDate != null)   colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (colCourse != null) colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));

        // Status column: value + colored text depending on the status.
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
                    // Left-align the text; adjust text colour by status.
                    setStyle("-fx-alignment: CENTER-LEFT;");
                    switch (lower) {
                        case "present" -> setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #2e7d32;");
                        case "late"    -> setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #f57c00;");
                        case "absent"  -> setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #c62828;");
                        case "pending" -> setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #616161;");
                        default        -> setStyle("-fx-alignment: CENTER-LEFT;");
                    }
                }
            });
        }

        // Remaining columns: standard property bindings.
        if (colMethod != null)   colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        if (colMarkedAt != null) colMarkedAt.setCellValueFactory(new PropertyValueFactory<>("markedAt"));

        // Row-level background color to match the legend.
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

            // Make columns fill the available width.
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
    }
}
