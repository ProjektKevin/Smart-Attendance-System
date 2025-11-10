// F_MA: modified by felicia handling marking attendance
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

import com.smartattendance.ApplicationContext;
import com.smartattendance.repository.AttendanceRecordRepository;

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
 * Assumes there is an AttendanceRow POJO with properties: - String module -
 * LocalDate date - String status (e.g., "Present" / "Absent")
 *
 * Replace the demo data loader with your real data source when you wire the DB.
 */
public class StudentAttendanceController {

    // Filters
    @FXML
    private ComboBox<String> courseCombo;
    @FXML
    private DatePicker fromDate, toDate;
    @FXML
    private Label recordsLabel;

    // Detail table
    @FXML
    private TableView<AttendanceRow> table;

    // Summary table
    @FXML
    private TableView<CourseSummary> summaryTable;
    @FXML
    private TableColumn<CourseSummary, Double> colPercent;

    // All rows (load once, then filter in-memory)
    private final ObservableList<AttendanceRow> masterData = FXCollections.observableArrayList();

    // Current filtered view for details
    private final ObservableList<AttendanceRow> filtered = FXCollections.observableArrayList();

    // Summary list
    private final ObservableList<CourseSummary> summary = FXCollections.observableArrayList();

    // User Id
    private final int userId = ApplicationContext.getAuthSession().getCurrentUser().getId();

    // Repository
    private final AttendanceRecordRepository attendanceRepository = new AttendanceRecordRepository();

    @FXML
    private void initialize() {
        // Demo data: replace with repository/API once available
        loadDataFromDatabase();

        // init filters
        Set<String> courses = masterData.stream().map(AttendanceRow::getCourseName).collect(Collectors.toCollection(TreeSet::new));
        courseCombo.getItems().setAll(courses);
        courseCombo.getItems().add(0, "All courses");
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
                    int pct = (int) Math.round((ratio.isNaN() ? 0 : ratio) * 100.0);
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

    // @FXML
    // private void onApplyFilters() {
    //     applyFilters();
    // }

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
                .filter(r -> (courseSel == null || courseSel.equals("All courses") || r.getCourseName().equals(courseSel)))
                .filter(r -> (from == null || !r.getSessionDate().isBefore(from)))
                .filter(r -> (to == null || !r.getSessionDate().isAfter(to)))
                .sorted(Comparator.comparing(AttendanceRow::getSessionDate).reversed())
                .toList());

        recordsLabel.setText(filtered.size() + " records");

        // recompute module summary over the *same* filtered set
        Map<String, List<AttendanceRow>> byCourse = filtered.stream()
                .collect(Collectors.groupingBy(AttendanceRow::getCourseName, TreeMap::new, Collectors.toList()));

        List<CourseSummary> rows = new ArrayList<>();
        byCourse.forEach((module, list) -> {
            long attended = list.stream().filter(r -> "Present".equalsIgnoreCase(r.getStatus())).count();
            int total = list.size();
            double ratio = total == 0 ? Double.NaN : (attended * 1.0 / total);
            rows.add(new CourseSummary(module, (int) attended, total, ratio));
        });

        summary.setAll(rows);
    }

    // --------------------------------------------------------------------
    // Real data 
    private void loadDataFromDatabase() {
        List<AttendanceRow> rows = attendanceRepository.findByStudentId(userId);
        masterData.setAll(rows);
    }

    // private void add(LocalDate sessionDate, String startTime, String endTime, String courseName, String status) {
    //     masterData.add(new AttendanceRow(sessionDate, startTime, endTime, courseName, status));
    // }

    // --------------------------------------------------------------------
    // View models
    /**
     * Minimal row used by the details table. Adjust if your real model differs.
     */
    public static class AttendanceRow {

        private final LocalDate sessionDate;
        private final String startTime;
        private final String endTime;
        private final String courseCode;
        private final String courseName;
        private final String status;

        public AttendanceRow(LocalDate sessionDate, String startTime, String endTime, String courseCode, String courseName, String status) {
            this.sessionDate = sessionDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.status = status;
        }

        public LocalDate getSessionDate() {
            return sessionDate;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getStatus() {
            return status;
        }
    }

    /**
     * Summary row for the per-module table.
     */
    public static class CourseSummary {

        private final String course;
        private final int attended;
        private final int total;
        private final double ratio; // 0..1 (NaN if total == 0)

        public CourseSummary(String course, int attended, int total, double ratio) {
            this.course = course;
            this.attended = attended;
            this.total = total;
            this.ratio = ratio;
        }

        public String getCourse() {
            return course;
        }

        public int getAttended() {
            return attended;
        }

        public int getTotal() {
            return total;
        }

        public Double getRatio() {
            return ratio;
        }
    }
}
