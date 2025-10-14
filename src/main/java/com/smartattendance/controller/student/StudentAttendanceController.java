package com.smartattendance.controller.student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

/**
 * Shows student's attendance with filters and a per-module percentage summary.
 * Assumes there is an AttendanceRow POJO with properties:
 *   - String module
 *   - LocalDate date
 *   - String status  (e.g., "Present" / "Absent")
 *
 * Replace the demo data loader with your real data source when you wire the DB.
 */
public class StudentAttendanceController {

    // Filters
    @FXML private ComboBox<String> courseCombo;
    @FXML private DatePicker fromDate, toDate;
    @FXML private Label recordsLabel;

    // Detail table
    @FXML private TableView<AttendanceRow> table;

    // Summary table
    @FXML private TableView<ModuleSummary> summaryTable;
    @FXML private TableColumn<ModuleSummary, Double> colPercent;

    // All rows (load once, then filter in-memory)
    private final ObservableList<AttendanceRow> masterData = FXCollections.observableArrayList();

    // Current filtered view for details
    private final ObservableList<AttendanceRow> filtered = FXCollections.observableArrayList();

    // Summary list
    private final ObservableList<ModuleSummary> summary = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Demo data: replace with repository/API once available
        loadDemoData();

        // init filters
        Set<String> modules = masterData.stream().map(AttendanceRow::getModule).collect(Collectors.toCollection(TreeSet::new));
        courseCombo.getItems().setAll(modules);
        courseCombo.getItems().add(0, "All modules");
        courseCombo.getSelectionModel().selectFirst();

        // default dates (last 90 days)
        toDate.setValue(LocalDate.now());
        fromDate.setValue(toDate.getValue().minusDays(90));

        // bind detail table & summary
        table.setItems(filtered);
        summaryTable.setItems(summary);

        // nice progress cell for percentage
        colPercent.setCellFactory(tc -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar();
            private final Label lbl = new Label();
            private final HBox box = new HBox(8, bar, lbl);
            {
                bar.setMaxWidth(Double.MAX_VALUE);
                box.setFillHeight(true);
            }
            @Override
            protected void updateItem(Double ratio, boolean empty) {
                super.updateItem(ratio, empty);
                if (empty || ratio == null) {
                    setGraphic(null);
                } else {
                    bar.setProgress(ratio.isNaN() ? 0 : ratio); // 0..1
                    int pct = (int)Math.round((ratio.isNaN() ? 0 : ratio) * 100.0);
                    lbl.setText(pct + "%");
                    setGraphic(box);
                }
            }
        });

        // apply once
        applyFilters();

        // react to quick filter changes (optional live update)
        courseCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        fromDate.valueProperty().addListener((obs, o, n) -> applyFilters());
        toDate.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    @FXML
    private void onApplyFilters() {
        applyFilters();
    }

    @FXML
    private void onResetFilters() {
        courseCombo.getSelectionModel().selectFirst();
        toDate.setValue(LocalDate.now());
        fromDate.setValue(toDate.getValue().minusDays(90));
        applyFilters();
    }

    private void applyFilters() {
        String courseSel = courseCombo.getSelectionModel().getSelectedItem();
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        filtered.setAll(masterData.stream()
                .filter(r -> (courseSel == null || courseSel.equals("All modules") || r.getModule().equals(courseSel)))
                .filter(r -> (from == null || !r.getDate().isBefore(from)))
                .filter(r -> (to == null   || !r.getDate().isAfter(to)))
                .sorted(Comparator.comparing(AttendanceRow::getDate).reversed())
                .toList());

        recordsLabel.setText(filtered.size() + " records");

        // recompute module summary over the *same* filtered set
        Map<String, List<AttendanceRow>> byModule = filtered.stream()
                .collect(Collectors.groupingBy(AttendanceRow::getModule, TreeMap::new, Collectors.toList()));

        List<ModuleSummary> rows = new ArrayList<>();
        byModule.forEach((module, list) -> {
            long attended = list.stream().filter(r -> "Present".equalsIgnoreCase(r.getStatus())).count();
            int total = list.size();
            double ratio = total == 0 ? Double.NaN : (attended * 1.0 / total);
            rows.add(new ModuleSummary(module, (int)attended, total, ratio));
        });

        summary.setAll(rows);
    }

    // --------------------------------------------------------------------
    // Demo data (replace with real repository / service later)
    private void loadDemoData() {
        // A few recent dates across two modules
        LocalDate today = LocalDate.now();
        add(today.minusDays(3),  "CS101 - Programming",   "Present");
        add(today.minusDays(10), "CS101 - Programming",   "Absent");
        add(today.minusDays(17), "CS101 - Programming",   "Present");
        add(today.minusDays(1),  "MTH120 - Calculus",     "Present");
        add(today.minusDays(8),  "MTH120 - Calculus",     "Present");
        add(today.minusDays(15), "MTH120 - Calculus",     "Absent");
        add(today.minusDays(22), "MTH120 - Calculus",     "Present");
        add(today.minusDays(5),  "PHY130 - Mechanics",    "Absent");
        add(today.minusDays(12), "PHY130 - Mechanics",    "Present");
        add(today.minusDays(19), "PHY130 - Mechanics",    "Present");
        add(today.minusDays(26), "PHY130 - Mechanics",    "Present");
    }

    private void add(LocalDate date, String module, String status) {
        masterData.add(new AttendanceRow(date, module, status));
    }

    // --------------------------------------------------------------------
    // View models

    /** Minimal row used by the details table. Adjust if your real model differs. */
    public static class AttendanceRow {
        private final LocalDate date;
        private final String module;
        private final String status;

        public AttendanceRow(LocalDate date, String module, String status) {
            this.date = date;
            this.module = module;
            this.status = status;
        }
        public LocalDate getDate()   { return date; }
        public String getModule()    { return module; }
        public String getStatus()    { return status; }
    }

    /** Summary row for the per-module table. */
    public static class ModuleSummary {
        private final String module;
        private final int attended;
        private final int total;
        private final double ratio; // 0..1 (NaN if total == 0)

        public ModuleSummary(String module, int attended, int total, double ratio) {
            this.module = module;
            this.attended = attended;
            this.total = total;
            this.ratio = ratio;
        }
        public String getModule()   { return module; }
        public int getAttended()    { return attended; }
        public int getTotal()       { return total; }
        public Double getRatio()    { return ratio; }
    }
}
