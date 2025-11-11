package com.smartattendance.controller;

import java.time.LocalDate;
import java.util.List;

import com.smartattendance.model.dto.dashboard.AttendanceRecord;
import com.smartattendance.model.dto.dashboard.DashboardFilter;
import com.smartattendance.model.dto.dashboard.DashboardTopCards;
import com.smartattendance.repository.DashboardRepository;
import com.smartattendance.repository.JdbcDashboardRepository;
import com.smartattendance.service.DashboardService;
import com.smartattendance.util.chart.PngChartExporter;
import com.smartattendance.util.dashboard.DashboardCharts;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dashboard controller that uses the dashboard util + chart util.
 */
public class DashboardController {

    // ===== FILTERS =====
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private ComboBox<String> courseCombo;
    @FXML private ComboBox<String> sessionCombo;
    @FXML private CheckBox onTimeChk;
    @FXML private CheckBox lateChk;
    @FXML private CheckBox absentChk;
    @FXML private CheckBox excusedChk;
    @FXML private Button   resetBtn;
    @FXML private Button   latestSessionBtn;

    // ===== CARDS =====
    @FXML private Label studentsCount;
    @FXML private Label sessionsCount;
    @FXML private Label presentTodayCount;

    // ===== CHARTS / TABLE =====
    @FXML private BarChart<String, Number> weeklyChart;
    @FXML private CategoryAxis            chartXAxis;
    @FXML private NumberAxis              chartYAxis;

    @FXML private TableView<ActivityItem> recentTable;
    @FXML private TableColumn<ActivityItem, String> colTime;
    @FXML private TableColumn<ActivityItem, String> colStudent;
    @FXML private TableColumn<ActivityItem, String> colStatus;

    @FXML private BarChart<String, Number> arrivalHistogram;
    @FXML private CategoryAxis             arrivalXAxis;
    @FXML private NumberAxis               arrivalYAxis;

    @FXML private PieChart statusPie;
    @FXML private HBox     statusLegendBox;

    @FXML private StackedBarChart<String, Number> courseChart;
    @FXML private CategoryAxis                    courseXAxis;
    @FXML private NumberAxis                      courseYAxis;
    @FXML private HBox                             courseLegendBox;

    // ===== DATA LAYER =====
    private final DashboardRepository dashboardRepository = new JdbcDashboardRepository();
    private final DashboardService    dashboardService    = new DashboardService(dashboardRepository);

    private LocalDate defaultFrom;
    private LocalDate defaultTo;

    @FXML
    public void initialize() {
        defaultFrom = LocalDate.now().minusDays(6);
        defaultTo   = LocalDate.now();

        if (fromDate != null) fromDate.setValue(defaultFrom);
        if (toDate   != null) toDate.setValue(defaultTo);

        loadCourseCombo();
        loadSessionCombo(null);

        if (onTimeChk  != null) onTimeChk.setSelected(true);
        if (lateChk    != null) lateChk.setSelected(true);
        if (absentChk  != null) absentChk.setSelected(true);
        if (excusedChk != null) excusedChk.setSelected(true);

        setupTableColumns();
        wireFilters();

        refreshAll();
    }

    /* ---------------------------------------------------------
     * WIRING
     * --------------------------------------------------------- */
    private void wireFilters() {
        if (fromDate != null) {
            fromDate.valueProperty().addListener((obs,o,n)->{
                clampDates();
                refreshAll();
            });
        }
        if (toDate != null) {
            toDate.valueProperty().addListener((obs,o,n)->{
                clampDates();
                refreshAll();
            });
        }
        if (courseCombo != null) {
            courseCombo.valueProperty().addListener((obs,o,n)->{
                Integer courseId = parseIdFromCombo(n);
                loadSessionCombo(courseId);
                refreshAll();
            });
        }
        if (sessionCombo != null) {
            sessionCombo.valueProperty().addListener((obs,o,n)-> refreshAll());
        }
        if (onTimeChk  != null) onTimeChk.selectedProperty().addListener((obs,o,n)-> refreshAll());
        if (lateChk    != null) lateChk.selectedProperty().addListener((obs,o,n)-> refreshAll());
        if (absentChk  != null) absentChk.selectedProperty().addListener((obs,o,n)-> refreshAll());
        if (excusedChk != null) excusedChk.selectedProperty().addListener((obs,o,n)-> refreshAll());

        if (resetBtn != null) {
            resetBtn.setOnAction(e -> {
                fromDate.setValue(defaultFrom);
                toDate.setValue(defaultTo);
                courseCombo.getSelectionModel().selectFirst();
                loadSessionCombo(null);
                onTimeChk.setSelected(true);
                lateChk.setSelected(true);
                absentChk.setSelected(true);
                excusedChk.setSelected(true);
                refreshAll();
            });
        }

        if (latestSessionBtn != null) {
            latestSessionBtn.setOnAction(e -> {
                try {
                    Integer courseId = parseIdFromCombo(courseCombo.getValue());
                    String latest = dashboardRepository.findLatestSessionLabel(courseId);
                    if (latest != null) {
                        sessionCombo.getSelectionModel().select(latest);
                        // DO NOT touch date filters
                        refreshAll();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    private void clampDates() {
        if (fromDate != null && toDate != null
                && fromDate.getValue() != null
                && toDate.getValue() != null
                && toDate.getValue().isBefore(fromDate.getValue())) {
            toDate.setValue(fromDate.getValue());
        }
    }

    /* ---------------------------------------------------------
     * LOAD COMBOS
     * --------------------------------------------------------- */
    private void loadCourseCombo() {
        if (courseCombo == null) return;
        try {
            courseCombo.getItems().setAll(dashboardRepository.listCourseLabels());
            courseCombo.getSelectionModel().selectFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSessionCombo(Integer courseId) {
        if (sessionCombo == null) return;
        try {
            sessionCombo.getItems().setAll(dashboardRepository.listSessionLabels(courseId));
            sessionCombo.getSelectionModel().selectFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ---------------------------------------------------------
     * REFRESH
     * --------------------------------------------------------- */
    private void refreshAll() {
        DashboardFilter filter = buildFilterFromUI();

        // attendance rows
        List<AttendanceRecord> records;
        try {
            records = dashboardService.loadAttendance(filter);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // top cards
        try {
            DashboardTopCards cards = dashboardService.computeTopCards(filter);
            if (studentsCount != null) studentsCount.setText(String.valueOf(cards.getStudents()));
            if (sessionsCount != null) sessionsCount.setText(String.valueOf(cards.getSessions()));
            if (presentTodayCount != null) presentTodayCount.setText(String.valueOf(cards.getPresent()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // table
        fillRecentTable(records);

        // charts (now real, via util/dashboard)
        DashboardCharts.setupWeeklyChart(
                weeklyChart, chartXAxis, chartYAxis,
                records, filter.from, filter.to
        );
        DashboardCharts.setupArrivalHistogram(
                arrivalHistogram, arrivalXAxis, arrivalYAxis,
                records
        );
        DashboardCharts.setupStatusPie(
                statusPie, statusLegendBox,
                records,
                filter.includePresent,
                filter.includeLate,
                filter.includeAbsent,
                filter.includePending
        );
        DashboardCharts.setupCourseChart(
                courseChart, courseXAxis, courseYAxis, courseLegendBox,
                records,
                filter.includePresent,
                filter.includeLate,
                filter.includeAbsent,
                filter.includePending
        );
    }

    private DashboardFilter buildFilterFromUI() {
        DashboardFilter f = new DashboardFilter();
        f.from = (fromDate != null) ? fromDate.getValue() : null;
        f.to   = (toDate   != null) ? toDate.getValue()   : null;
        f.courseId  = parseIdFromCombo(courseCombo  != null ? courseCombo.getValue()  : null);
        f.sessionId = parseIdFromCombo(sessionCombo != null ? sessionCombo.getValue() : null);

        f.includePresent = onTimeChk  == null || onTimeChk.isSelected();
        f.includeLate    = lateChk    == null || lateChk.isSelected();
        f.includeAbsent  = absentChk  == null || absentChk.isSelected();
        f.includePending = excusedChk == null || excusedChk.isSelected();
        return f;
    }

    /* ---------------------------------------------------------
     * TABLE
     * --------------------------------------------------------- */
    private void setupTableColumns() {
        if (recentTable == null) return;
        if (colTime != null)    colTime.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().time));
        if (colStudent != null) colStudent.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().student));
        if (colStatus != null)  colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().status));
    }

    private void fillRecentTable(List<AttendanceRecord> records) {
        if (recentTable == null) return;
        var rows = records.stream()
                .limit(40)
                .map(r -> new ActivityItem(
                        r.markedAt != null ? r.markedAt.toString().replace('T',' ') : "-",
                        r.username != null ? r.username : "Student " + r.userId,
                        r.status != null ? r.status : "Pending"
                ))
                .toList();
        recentTable.setItems(FXCollections.observableArrayList(rows));
    }

    /* ---------------------------------------------------------
     * EXPORT
     * --------------------------------------------------------- */
    @FXML
    private void exportWeeklyPng() {
        if (weeklyChart != null) {
            PngChartExporter.exportNode(weeklyChart, "weekly-attendance");
        }
    }

    @FXML
    private void exportArrivalPng() {
        if (arrivalHistogram != null) {
            PngChartExporter.exportNode(arrivalHistogram, "arrival-distribution");
        }
    }

    @FXML
    private void exportStatusPng() {
        if (statusPie != null) {
            PngChartExporter.exportNode(statusPie, "status-split");
        }
    }

    @FXML
    private void exportCoursePng() {
        if (courseChart != null) {
            PngChartExporter.exportNode(courseChart, "attendance-by-course");
        }
    }

    /* ---------------------------------------------------------
     * ZOOM (snapshot so original chart stays)
     * --------------------------------------------------------- */
    @FXML
    private void zoomWeekly()  { openZoomSnapshot(weeklyChart,  "Attendance (This Week)"); }
    @FXML
    private void zoomArrival() { openZoomSnapshot(arrivalHistogram, "Arrival Time Distribution"); }
    @FXML
    private void zoomPie()     { openZoomSnapshot(statusPie,   "Today's Status Split"); }
    @FXML
    private void zoomCourse()  { openZoomSnapshot(courseChart, "Attendance by Course"); }

    private void openZoomSnapshot(javafx.scene.Node node, String title) {
        if (node == null) return;

        // take a high-res snapshot
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(javafx.scene.transform.Transform.scale(1.5, 1.5));
        WritableImage img = node.snapshot(params, null);

        ImageView view = new ImageView(img);
        view.setPreserveRatio(true);
        view.setFitWidth(850);

        BorderPane root = new BorderPane(view);
        root.setPadding(new javafx.geometry.Insets(12));
        HBox top = new HBox(10, new Label(title));
        top.setPadding(new javafx.geometry.Insets(0,0,8,0));
        root.setTop(top);

        Scene scene = new Scene(root, 900, 600);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    /* ---------------------------------------------------------
     * helpers
     * --------------------------------------------------------- */
    private Integer parseIdFromCombo(String val) {
        if (val == null || val.equals("All")) return null;
        String[] parts = val.split(" - ", 2);
        try {
            return Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // simple table item
    public static class ActivityItem {
        public final String time;
        public final String student;
        public final String status;
        public ActivityItem(String time, String student, String status) {
            this.time = time;
            this.student = student;
            this.status = status;
        }
    }
}
