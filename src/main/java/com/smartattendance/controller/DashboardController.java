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
 * Controller for the main dashboard screen.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Read filters from the UI (dates, course, session, status checkboxes)</li>
 *     <li>Use {@link DashboardService} to load attendance records and compute top cards</li>
 *     <li>Populate charts via {@link DashboardCharts}</li>
 *     <li>Populate the recent activity table</li>
 *     <li>Handle PNG export and zoom views for charts</li>
 * </ul>
 *
 * <p>
 * Data access is delegated to {@link DashboardRepository} and
 * {@link DashboardService}. This controller focuses on wiring UI events to
 * service calls and updating JavaFX components.
 * </p>
 *
 * @author Ernest Lun
 */
public class DashboardController {

    // ===== FILTERS =====

    /** Start date filter for the dashboard. */
    @FXML private DatePicker fromDate;
    /** End date filter for the dashboard. */
    @FXML private DatePicker toDate;
    /** Course filter combo box (items like "1 - DAAA123"). */
    @FXML private ComboBox<String> courseCombo;
    /** Session filter combo box for the selected course. */
    @FXML private ComboBox<String> sessionCombo;
    /** Include "Present" / on-time records in charts and counts. */
    @FXML private CheckBox onTimeChk;
    /** Include "Late" records in charts and counts. */
    @FXML private CheckBox lateChk;
    /** Include "Absent" records in charts and counts. */
    @FXML private CheckBox absentChk;
    /** Include "Pending/Excused" records in charts and counts. */
    @FXML private CheckBox excusedChk;
    /** Button to reset all filters back to default values. */
    @FXML private Button   resetBtn;
    /** Button to jump to the latest session for the selected course. */
    @FXML private Button   latestSessionBtn;

    // ===== CARDS =====

    /** Card showing number of unique students in the filtered range. */
    @FXML private Label studentsCount;
    /** Card showing number of sessions in the filtered range. */
    @FXML private Label sessionsCount;
    /** Card showing number of present students today (or filtered range). */
    @FXML private Label presentTodayCount;

    // ===== CHARTS / TABLE =====

    /** Weekly bar chart of attendance over the selected date range. */
    @FXML private BarChart<String, Number> weeklyChart;
    @FXML private CategoryAxis            chartXAxis;
    @FXML private NumberAxis              chartYAxis;

    /** Recent activity table listing the latest attendance events. */
    @FXML private TableView<ActivityItem> recentTable;
    @FXML private TableColumn<ActivityItem, String> colTime;
    @FXML private TableColumn<ActivityItem, String> colStudent;
    @FXML private TableColumn<ActivityItem, String> colStatus;

    /** Histogram of arrival times (e.g. early, on time, late buckets). */
    @FXML private BarChart<String, Number> arrivalHistogram;
    @FXML private CategoryAxis             arrivalXAxis;
    @FXML private NumberAxis               arrivalYAxis;

    /** Pie chart of attendance status distribution. */
    @FXML private PieChart statusPie;
    /** Legend container for the status pie chart. */
    @FXML private HBox     statusLegendBox;

    /** Stacked bar chart of attendance by course. */
    @FXML private StackedBarChart<String, Number> courseChart;
    @FXML private CategoryAxis                    courseXAxis;
    @FXML private NumberAxis                      courseYAxis;
    /** Legend container for the course chart. */
    @FXML private HBox                            courseLegendBox;

    // ===== DATA LAYER =====

    /** Repository implementation used by the dashboard service. */
    private final DashboardRepository dashboardRepository = new JdbcDashboardRepository();
    /** Service that encapsulates dashboard business logic. */
    private final DashboardService    dashboardService    = new DashboardService(dashboardRepository);

    /** Default "from" date (usually 7-day window ending today). */
    private LocalDate defaultFrom;
    /** Default "to" date (today). */
    private LocalDate defaultTo;

    /**
     * JavaFX lifecycle hook invoked after FXML fields are injected.
     *
     * <p>
     * This method:
     * <ul>
     *     <li>Initialises default date range</li>
     *     <li>Loads course and session combo box options</li>
     *     <li>Sets default status checkboxes</li>
     *     <li>Configures the recent activity table columns</li>
     *     <li>Wires filter controls to trigger refreshes</li>
     *     <li>Performs an initial {@link #refreshAll()} to populate the dashboard</li>
     * </ul>
     * </p>
     */
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

    /**
     * Wires filter controls so that changes automatically re-apply
     * the dashboard filter and refresh all widgets.
     *
     * <p>
     * Listeners are attached to:
     * <ul>
     *     <li>Date pickers (from/to)</li>
     *     <li>Course and session combo boxes</li>
     *     <li>Status checkboxes</li>
     *     <li>Reset and "latest session" buttons</li>
     * </ul>
     * </p>
     */
    private void wireFilters() {
        if (fromDate != null) {
            fromDate.valueProperty().addListener((obs, o, n) -> {
                clampDates();
                refreshAll();
            });
        }
        if (toDate != null) {
            toDate.valueProperty().addListener((obs, o, n) -> {
                clampDates();
                refreshAll();
            });
        }
        if (courseCombo != null) {
            courseCombo.valueProperty().addListener((obs, o, n) -> {
                Integer courseId = parseIdFromCombo(n);
                loadSessionCombo(courseId);
                refreshAll();
            });
        }
        if (sessionCombo != null) {
            sessionCombo.valueProperty().addListener((obs, o, n) -> refreshAll());
        }
        if (onTimeChk  != null) onTimeChk.selectedProperty().addListener((obs, o, n) -> refreshAll());
        if (lateChk    != null) lateChk.selectedProperty().addListener((obs, o, n) -> refreshAll());
        if (absentChk  != null) absentChk.selectedProperty().addListener((obs, o, n) -> refreshAll());
        if (excusedChk != null) excusedChk.selectedProperty().addListener((obs, o, n) -> refreshAll());

        // Reset all filters back to default.
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

        // Jump to the latest session for the currently selected course.
        if (latestSessionBtn != null) {
            latestSessionBtn.setOnAction(e -> {
                try {
                    Integer courseId = parseIdFromCombo(courseCombo.getValue());
                    String latest = dashboardRepository.findLatestSessionLabel(courseId);
                    if (latest != null) {
                        sessionCombo.getSelectionModel().select(latest);
                        // Do not touch date filters; only change the session.
                        refreshAll();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    /**
     * Ensures the "to" date is not earlier than the "from" date.
     *
     * <p>
     * If the user picks an invalid range (to &lt; from), the "to" date
     * is clamped to match the "from" date.
     * </p>
     */
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

    /**
     * Loads course labels into the course combo box and selects the first entry.
     *
     * <p>
     * Labels are retrieved from {@link DashboardRepository#listCourseLabels()}.
     * </p>
     */
    private void loadCourseCombo() {
        if (courseCombo == null) return;
        try {
            courseCombo.getItems().setAll(dashboardRepository.listCourseLabels());
            courseCombo.getSelectionModel().selectFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads session labels into the session combo box for the given course.
     *
     * @param courseId course identifier, or {@code null} to list sessions
     *                 without filtering by course
     */
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

    /**
     * Recomputes and refreshes all dashboard widgets based on the
     * current UI filter state.
     *
     * <p>
     * This method:
     * <ul>
     *     <li>Builds a {@link DashboardFilter}</li>
     *     <li>Loads attendance records</li>
     *     <li>Updates top cards</li>
     *     <li>Fills the recent activity table</li>
     *     <li>Delegates to {@link DashboardCharts} to rebuild charts</li>
     * </ul>
     * </p>
     */
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

        // charts (via util/dashboard)
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

    /**
     * Builds a {@link DashboardFilter} object from the current UI state.
     *
     * <p>
     * Includes date range, course and session IDs parsed from combo labels,
     * and the status flags from the checkboxes.
     * </p>
     *
     * @return populated {@link DashboardFilter} instance
     */
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

    /**
     * Configures the recent activity table columns to read directly from
     * {@link ActivityItem} fields.
     */
    private void setupTableColumns() {
        if (recentTable == null) return;
        if (colTime != null) {
            colTime.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().time));
        }
        if (colStudent != null) {
            colStudent.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().student));
        }
        if (colStatus != null) {
            colStatus.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().status));
        }
    }

    /**
     * Populates the recent activity table with up to 40 most recent records.
     *
     * @param records attendance records loaded from the service
     */
    private void fillRecentTable(List<AttendanceRecord> records) {
        if (recentTable == null) return;
        var rows = records.stream()
                .limit(40)
                .map(r -> new ActivityItem(
                        r.markedAt != null ? r.markedAt.toString().replace('T', ' ') : "-",
                        r.username != null ? r.username : "Student " + r.userId,
                        r.status != null ? r.status : "Pending"
                ))
                .toList();
        recentTable.setItems(FXCollections.observableArrayList(rows));
    }

    /* ---------------------------------------------------------
     * EXPORT
     * --------------------------------------------------------- */

    /** Exports the weekly chart as a PNG image. */
    @FXML
    private void exportWeeklyPng() {
        if (weeklyChart != null) {
            PngChartExporter.exportNode(weeklyChart, "weekly-attendance");
        }
    }

    /** Exports the arrival histogram as a PNG image. */
    @FXML
    private void exportArrivalPng() {
        if (arrivalHistogram != null) {
            PngChartExporter.exportNode(arrivalHistogram, "arrival-distribution");
        }
    }

    /** Exports the status pie chart as a PNG image. */
    @FXML
    private void exportStatusPng() {
        if (statusPie != null) {
            PngChartExporter.exportNode(statusPie, "status-split");
        }
    }

    /** Exports the course stacked bar chart as a PNG image. */
    @FXML
    private void exportCoursePng() {
        if (courseChart != null) {
            PngChartExporter.exportNode(courseChart, "attendance-by-course");
        }
    }

    /* ---------------------------------------------------------
     * ZOOM (snapshot so original chart stays)
     * --------------------------------------------------------- */

    /** Opens a zoomed snapshot window for the weekly chart. */
    @FXML
    private void zoomWeekly()  { openZoomSnapshot(weeklyChart,  "Attendance (This Week)"); }

    /** Opens a zoomed snapshot window for the arrival histogram. */
    @FXML
    private void zoomArrival() { openZoomSnapshot(arrivalHistogram, "Arrival Time Distribution"); }

    /** Opens a zoomed snapshot window for the status pie chart. */
    @FXML
    private void zoomPie()     { openZoomSnapshot(statusPie,   "Today's Status Split"); }

    /** Opens a zoomed snapshot window for the course stacked bar chart. */
    @FXML
    private void zoomCourse()  { openZoomSnapshot(courseChart, "Attendance by Course"); }

    /**
     * Takes a high-resolution snapshot of the given chart/node and shows it
     * in a modal dialog for easier viewing.
     *
     * @param node  chart or node to snapshot
     * @param title window title for the zoom dialog
     */
    private void openZoomSnapshot(javafx.scene.Node node, String title) {
        if (node == null) return;

        // Take a high-res snapshot (scale up for clarity).
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(javafx.scene.transform.Transform.scale(1.5, 1.5));
        WritableImage img = node.snapshot(params, null);

        ImageView view = new ImageView(img);
        view.setPreserveRatio(true);
        view.setFitWidth(850);

        BorderPane root = new BorderPane(view);
        root.setPadding(new javafx.geometry.Insets(12));
        HBox top = new HBox(10, new Label(title));
        top.setPadding(new javafx.geometry.Insets(0, 0, 8, 0));
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

    /**
     * Parses an integer ID from a combo box label in the form
     * {@code "123 - COURSE NAME"}.
     *
     * @param val the combo box string value
     * @return parsed integer ID, or {@code null} if not parseable or "All"
     */
    private Integer parseIdFromCombo(String val) {
        if (val == null || val.equals("All")) return null;
        String[] parts = val.split(" - ", 2);
        try {
            return Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Simple DTO used as the backing item for the recent activity table.
     */
    public static class ActivityItem {
        /** Display time (formatted from {@code markedAt}). */
        public final String time;
        /** Display name for the student. */
        public final String student;
        /** Attendance status label. */
        public final String status;

        /**
         * Constructs an immutable activity item.
         *
         * @param time    display time string
         * @param student display student name or fallback
         * @param status  attendance status
         */
        public ActivityItem(String time, String student, String status) {
            this.time = time;
            this.student = student;
            this.status = status;
        }
    }
}
