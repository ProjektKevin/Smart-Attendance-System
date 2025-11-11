package com.smartattendance.controller.student;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.User;
import com.smartattendance.service.StudentAttendanceService;

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

public class StudentAttendanceController {

    // filters
    @FXML private ComboBox<String> courseCombo;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Label recordsLabel;

    // tables
    @FXML private TableView<StudentAttendanceRow> table;
    @FXML private TableView<StudentModuleSummary> summaryTable;

    // detail columns
    @FXML private TableColumn<StudentAttendanceRow, LocalDate> colDate;
    @FXML private TableColumn<StudentAttendanceRow, String>    colModule;
    @FXML private TableColumn<StudentAttendanceRow, String>    colStatus;
    @FXML private TableColumn<StudentAttendanceRow, String>    colMethod;
    @FXML private TableColumn<StudentAttendanceRow, String>    colMarkedAt;

    // summary columns
    @FXML private TableColumn<StudentModuleSummary, String>  colSummaryModule;
    @FXML private TableColumn<StudentModuleSummary, Integer> colSummaryAttended;
    @FXML private TableColumn<StudentModuleSummary, Integer> colSummaryTotal;
    @FXML private TableColumn<StudentModuleSummary, Double>  colSummaryPercent;

    private final ObservableList<StudentAttendanceRow> masterData = FXCollections.observableArrayList();
    private final ObservableList<StudentAttendanceRow> filtered   = FXCollections.observableArrayList();
    private final ObservableList<StudentModuleSummary> summary    = FXCollections.observableArrayList();

    private final StudentAttendanceService service = new StudentAttendanceService();

    @FXML
    private void initialize() {
        System.out.println("[StudentAttendance] init");

        // ---- detail table setup ----
        if (colDate != null)   colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (colModule != null) colModule.setCellValueFactory(new PropertyValueFactory<>("module"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (colMethod != null) colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        if (colMarkedAt != null) colMarkedAt.setCellValueFactory(new PropertyValueFactory<>("markedAt"));

        // row color coding (matches legend in FXML)
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
        if (colSummaryModule != null)
            colSummaryModule.setCellValueFactory(new PropertyValueFactory<>("module"));
        if (colSummaryAttended != null)
            colSummaryAttended.setCellValueFactory(new PropertyValueFactory<>("attended"));
        if (colSummaryTotal != null)
            colSummaryTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

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

        // load data for logged-in student
        User current = ApplicationContext.getAuthSession().getCurrentUser();
        if (current != null) {
            masterData.setAll(service.loadRowsForStudent(current.getId()));
        }

        // bind tables
        if (table != null)        table.setItems(filtered);
        if (summaryTable != null) summaryTable.setItems(summary);

        // default dates
        LocalDate today = LocalDate.now();
        if (toDate != null)   toDate.setValue(today);
        if (fromDate != null) fromDate.setValue(today.minusDays(90));

        // populate course combo
        Set<String> modules = masterData.stream()
                .map(StudentAttendanceRow::getModule)
                .collect(Collectors.toCollection(TreeSet::new));
        if (courseCombo != null) {
            courseCombo.getItems().setAll(modules);
            courseCombo.getItems().add(0, "All modules");
            courseCombo.getSelectionModel().selectFirst();
            courseCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        }

        // auto-apply on date change
        if (fromDate != null) fromDate.valueProperty().addListener((obs, o, n) -> applyFilters());
        if (toDate != null)   toDate.valueProperty().addListener((obs, o, n) -> applyFilters());

        applyFilters();
    }

    @FXML
    private void onApplyFilters() {
        applyFilters();
    }

    @FXML
    private void onResetFilters() {
        LocalDate today = LocalDate.now();
        if (courseCombo != null && !courseCombo.getItems().isEmpty())
            courseCombo.getSelectionModel().selectFirst();
        if (fromDate != null) fromDate.setValue(today.minusDays(90));
        if (toDate != null)   toDate.setValue(today);
        applyFilters();
    }

    private void applyFilters() {
        String module = (courseCombo != null) ? courseCombo.getSelectionModel().getSelectedItem() : null;
        LocalDate from = (fromDate != null) ? fromDate.getValue() : null;
        LocalDate to   = (toDate != null)   ? toDate.getValue()   : null;

        // small guard: if user picks reversed dates, swap
        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
            if (fromDate != null) fromDate.setValue(from);
            if (toDate != null)   toDate.setValue(to);
        }

        var result = service.applyFilters(masterData, module, from, to);
        filtered.setAll(result.getRows());
        summary.setAll(result.getSummaries());

        if (recordsLabel != null) {
            recordsLabel.setText(result.getRows().size() + " records");
        }
    }
}
