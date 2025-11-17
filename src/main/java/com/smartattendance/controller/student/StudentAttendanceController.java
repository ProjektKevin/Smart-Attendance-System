package com.smartattendance.controller.student;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.User;
import com.smartattendance.service.StudentAttendanceService;
import com.smartattendance.util.security.log.ApplicationLogger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Controller for the student-facing attendance screen.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Load attendance records for the currently logged-in student</li>
 *     <li>Apply filters by course and date range</li>
 *     <li>Populate and style the detailed attendance table</li>
 *     <li>Populate the course-level summary table with progress bars</li>
 *     <li>Update the record count label based on active filters</li>
 * </ul>
 *
 * <p>
 * All data fetching and filter logic is delegated to
 * {@link StudentAttendanceService}, while this class focuses on wiring the
 * UI controls (JavaFX) to the underlying data model.
 * </p>
 *
 * @author Ernest Lun
 */
public class StudentAttendanceController {

    // ===== filter controls =====

    /**
     * Course selection combo box. The first item is typically "All courses".
     */
    @FXML
    private ComboBox<String> courseCombo;

    /**
     * Start of the date range filter.
     */
    @FXML
    private DatePicker fromDate;

    /**
     * End of the date range filter.
     */
    @FXML
    private DatePicker toDate;

    /**
     * Label used to display the number of matching attendance records.
     */
    @FXML
    private Label recordsLabel;

    // ===== tables =====

    /**
     * Table showing detailed per-session attendance records.
     */
    @FXML
    private TableView<StudentAttendanceRow> table;

    /**
     * Table showing course-level attendance summary (with progress bars).
     */
    @FXML
    private TableView<StudentCourseSummary> summaryTable;

    // ===== detail table columns =====

    /**
     * Column for the attendance date.
     */
    @FXML
    private TableColumn<StudentAttendanceRow, LocalDate> colDate;

    /**
     * Column for the course name/code.
     */
    @FXML
    private TableColumn<StudentAttendanceRow, String> colCourse;

    /**
     * Column for the attendance status (present, late, absent, pending, etc.).
     */
    @FXML
    private TableColumn<StudentAttendanceRow, String> colStatus;

    /**
     * Column for the attendance method (e.g. Face, Manual).
     */
    @FXML
    private TableColumn<StudentAttendanceRow, String> colMethod;

    /**
     * Column for the timestamp at which attendance was marked.
     */
    @FXML
    private TableColumn<StudentAttendanceRow, String> colMarkedAt;

    // ===== summary table columns =====

    /**
     * Column for the course name in the summary table.
     */
    @FXML
    private TableColumn<StudentCourseSummary, String> colSummaryCourse;

    /**
     * Column for the number of attended sessions in the summary table.
     */
    @FXML
    private TableColumn<StudentCourseSummary, Integer> colSummaryAttended;

    /**
     * Column for the total number of sessions in the summary table.
     */
    @FXML
    private TableColumn<StudentCourseSummary, Integer> colSummaryTotal;

    /**
     * Column for the attendance ratio (0.0 - 1.0) rendered as a progress bar.
     */
    @FXML
    private TableColumn<StudentCourseSummary, Double> colSummaryPercent;

    // ===== backing lists =====

    /**
     * Full list of attendance rows for the current student (unfiltered).
     */
    private final ObservableList<StudentAttendanceRow> masterData =
            FXCollections.observableArrayList();

    /**
     * Filtered view of {@link #masterData} based on current filters.
     */
    private final ObservableList<StudentAttendanceRow> filtered =
            FXCollections.observableArrayList();

    /**
     * Summary rows (one per course) based on the current filtered data.
     */
    private final ObservableList<StudentCourseSummary> summary =
            FXCollections.observableArrayList();

    /**
     * Service used to load and filter student attendance records.
     */
    private final StudentAttendanceService service = new StudentAttendanceService();

    /**
     * Application-wide logger used for debug and audit messages.
     */
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    /**
     * JavaFX lifecycle hook invoked after FXML injection.
     *
     * <p>
     * This method:
     * <ul>
     *     <li>Configures table columns and cell factories</li>
     *     <li>Applies row styling based on attendance status</li>
     *     <li>Loads attendance data for the current user</li>
     *     <li>Configures filter controls (course combo and date pickers)</li>
     *     <li>Binds tables to observable lists and applies initial filters</li>
     * </ul>
     * </p>
     */
    @FXML
    private void initialize() {
        appLogger.info("[StudentAttendance] init");

        // ---- detail table setup ----
        if (colDate != null)   colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (colCourse != null) colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (colMethod != null) colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        if (colMarkedAt != null) colMarkedAt.setCellValueFactory(new PropertyValueFactory<>("markedAt"));

        // Row color coding (matches legend in FXML).
        // Each row gets a different background color based on the attendance status.
        if (table != null) {
            table.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(StudentAttendanceRow row, boolean empty) {
                    super.updateItem(row, empty);
                    if (empty || row == null) {
                        setStyle("");
                        return;
                    }
                    String s = row.getStatus() == null ? "" : row.getStatus().toLowerCase();
                    switch (s) {
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

        // ---- summary table setup (progress bar) ----
        if (colSummaryCourse != null)
            colSummaryCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        if (colSummaryAttended != null)
            colSummaryAttended.setCellValueFactory(new PropertyValueFactory<>("attended"));
        if (colSummaryTotal != null)
            colSummaryTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Render the percentage column as a progress bar plus percentage label.
        if (colSummaryPercent != null) {
            colSummaryPercent.setCellValueFactory(new PropertyValueFactory<>("ratio"));
            colSummaryPercent.setCellFactory(tc -> new TableCell<>() {
                private final ProgressBar bar = new ProgressBar();
                private final Label lbl = new Label();

                {
                    bar.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(bar, Priority.ALWAYS);
                }

                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null || value.isNaN()) {
                        setGraphic(null);
                    } else {
                        double p = Math.max(0, Math.min(1, value));
                        bar.setProgress(p);
                        lbl.setText(String.format("%.0f%%", p * 100));
                        setGraphic(new HBox(8, bar, lbl));
                    }
                }
            });
        }

        if (summaryTable != null) {
            summaryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        // Load data for the logged-in student.
        User current = ApplicationContext.getAuthSession().getCurrentUser();
        if (current != null) {
            masterData.setAll(service.loadRowsForStudent(current.getId()));
        }

        // Bind observable lists to tables.
        if (table != null)        table.setItems(filtered);
        if (summaryTable != null) summaryTable.setItems(summary);

        // Default date range: last 90 days until today.
        LocalDate today = LocalDate.now();
        if (toDate != null)   toDate.setValue(today);
        if (fromDate != null) fromDate.setValue(today.minusDays(90));

        // Populate course combo with all distinct courses from masterData.
        Set<String> courses = masterData.stream()
                .map(StudentAttendanceRow::getCourse)
                .collect(Collectors.toCollection(TreeSet::new));
        if (courseCombo != null) {
            courseCombo.getItems().setAll(courses);
            courseCombo.getItems().add(0, "All courses");
            courseCombo.getSelectionModel().selectFirst();
            // Re-apply filters whenever the course selection changes.
            courseCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        }

        // Re-apply filters when the date range changes.
        if (fromDate != null) fromDate.valueProperty().addListener((obs, o, n) -> applyFilters());
        if (toDate != null)   toDate.valueProperty().addListener((obs, o, n) -> applyFilters());

        // Initial filter application.
        applyFilters();
    }

    /**
     * Handler for the "Apply filters" button.
     *
     * <p>
     * Simply delegates to {@link #applyFilters()} so filters can also be
     * applied automatically when controls change.
     * </p>
     */
    @FXML
    private void onApplyFilters() {
        applyFilters();
    }

    /**
     * Handler for the "Reset filters" button.
     *
     * <p>
     * Resets the course selection to "All courses" and the date range
     * back to the last 90 days, then reapplies the filters.
     * </p>
     */
    @FXML
    private void onResetFilters() {
        LocalDate today = LocalDate.now();
        if (courseCombo != null && !courseCombo.getItems().isEmpty())
            courseCombo.getSelectionModel().selectFirst();
        if (fromDate != null) fromDate.setValue(today.minusDays(90));
        if (toDate != null)   toDate.setValue(today);
        applyFilters();
    }

    /**
     * Applies the current course and date filters to {@link #masterData},
     * updates the filtered detail list and summary list, and refreshes
     * the record count label.
     *
     * <p>
     * If the user selects a reversed date range (from &gt; to), the dates
     * are swapped and the controls are updated to reflect the corrected range.
     * </p>
     */
    private void applyFilters() {
        String course = (courseCombo != null)
                ? courseCombo.getSelectionModel().getSelectedItem()
                : null;
        LocalDate from = (fromDate != null) ? fromDate.getValue() : null;
        LocalDate to   = (toDate != null)   ? toDate.getValue()   : null;

        // Small guard: if user picks reversed dates, swap them.
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
            if (fromDate != null) fromDate.setValue(from);
            if (toDate != null)   toDate.setValue(to);
        }

        var result = service.applyFilters(masterData, course, from, to);
        filtered.setAll(result.getRows());
        summary.setAll(result.getSummaries());

        if (recordsLabel != null) {
            recordsLabel.setText(result.getRows().size() + " records");
        }
    }
}
