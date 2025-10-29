package com.smartattendance.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import com.smartattendance.model.entity.Student;
import com.smartattendance.service.StudentService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DashboardController {

    // ===== FILTERS =====
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private ComboBox<String> courseCombo;
    @FXML private ComboBox<String> groupCombo;
    @FXML private CheckBox onTimeChk;
    @FXML private CheckBox lateChk;
    @FXML private CheckBox absentChk;
    @FXML private CheckBox excusedChk;
    @FXML private Button   resetBtn;

    // ===== Top metric cards =====
    @FXML private Label studentsCount;
    @FXML private Label sessionsCount;
    @FXML private Label presentTodayCount;

    // ===== Weekly attendance chart =====
    @FXML private BarChart<String, Number> weeklyChart;
    @FXML private CategoryAxis chartXAxis;
    @FXML private NumberAxis chartYAxis;

    // ===== Recent activity table =====
    @FXML private TableView<ActivityItem> recentTable;
    @FXML private TableColumn<ActivityItem, String> colTime;
    @FXML private TableColumn<ActivityItem, String> colStudent;
    @FXML private TableColumn<ActivityItem, String> colStatus;

    // ===== Arrival histogram =====
    @FXML private BarChart<String, Number> arrivalHistogram;
    @FXML private CategoryAxis arrivalXAxis;
    @FXML private NumberAxis arrivalYAxis;

    // ===== Status pie (dashboard card) + its external legend row =====
    @FXML private PieChart statusPie;
    @FXML private HBox statusLegendBox;

    // ===== Attendance by course (stacked bar) + its external legend row =====
    @FXML private StackedBarChart<String, Number> courseChart;
    @FXML private CategoryAxis courseXAxis;
    @FXML private NumberAxis courseYAxis;
    @FXML private HBox courseLegendBox;

    private final StudentService studentService = new StudentService();

    private LocalDate defaultFrom;
    private LocalDate defaultTo;

    /** Single source of truth for all chart colors (order = series order). */
    private static final String[] SERIES_COLORS = {
        "#10b981", // On-time (green)
        "#f59e0b", // Late (amber)
        "#ef4444", // Absent (red)
        "#94a3b8", // Excused / neutral
        "#3b82f6"  // spare (blue)
    };

    @FXML
    public void initialize() {
        int totalStudents = studentService.getAllStudents().size();
        studentsCount.setText(String.valueOf(totalStudents));
        sessionsCount.setText(String.valueOf(3));
        presentTodayCount.setText(String.valueOf(Math.min(totalStudents, 2)));

        setupWeeklyChart(LocalDate.now().minusDays(6), LocalDate.now());
        setupRecentTable();
        setupArrivalHistogram();
        setupStatusPie(7, 3, 2, 0);
        setupCourseChart(new int[]{8,5,6}, new int[]{2,1,2}, new int[]{1,2,1});

        polishNumberAxis(chartYAxis);
        polishNumberAxis(arrivalYAxis);
        polishNumberAxis(courseYAxis);

        weeklyChart.setAnimated(false);
        weeklyChart.setLegendVisible(false);
        weeklyChart.setCategoryGap(12);
        weeklyChart.setBarGap(4);

        arrivalHistogram.setAnimated(false);
        arrivalHistogram.setLegendVisible(false);
        arrivalHistogram.setCategoryGap(10);
        arrivalHistogram.setBarGap(3);

        // In the *cards*, we render our own legend rows:
        statusPie.setLegendVisible(false);
        courseChart.setLegendVisible(false);

        addBarValueLabels(weeklyChart);
        addBarValueLabels(arrivalHistogram);
        wireTooltips();

        Platform.runLater(() -> {
            applyPalette(weeklyChart);
            applyPalette(arrivalHistogram);
            applyPalette(statusPie);
            applyPalette(courseChart);
            rebuildExternalPieLegend();
            rebuildExternalCourseLegend();
        });

        setupFilters();
        refreshAll();
    }

    /* ==================== Filters ==================== */

    private void setupFilters() {
        defaultFrom = LocalDate.now().minusDays(6);
        defaultTo   = LocalDate.now();

        if (fromDate != null) fromDate.setValue(defaultFrom);
        if (toDate   != null) toDate.setValue(defaultTo);

        if (courseCombo != null) {
            courseCombo.setItems(FXCollections.observableArrayList("All","CS102","MA101","PHY110"));
            courseCombo.getSelectionModel().selectFirst();
        }
        if (groupCombo != null) {
            groupCombo.setItems(FXCollections.observableArrayList("All","G1","G2","G3"));
            groupCombo.getSelectionModel().selectFirst();
        }
        if (onTimeChk != null) onTimeChk.setSelected(true);
        if (lateChk   != null) lateChk.setSelected(true);
        if (absentChk != null) absentChk.setSelected(true);
        if (excusedChk!= null) excusedChk.setSelected(true);

        if (fromDate != null) fromDate.valueProperty().addListener((o,a,b)->clampRangeAndRefresh());
        if (toDate   != null) toDate.valueProperty().addListener((o,a,b)->clampRangeAndRefresh());
        if (courseCombo != null) courseCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b)->refreshAll());
        if (groupCombo  != null) groupCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b)->refreshAll());
        if (onTimeChk   != null) onTimeChk.selectedProperty().addListener((o,a,b)->refreshAll());
        if (lateChk     != null) lateChk.selectedProperty().addListener((o,a,b)->refreshAll());
        if (absentChk   != null) absentChk.selectedProperty().addListener((o,a,b)->refreshAll());
        if (excusedChk  != null) excusedChk.selectedProperty().addListener((o,a,b)->refreshAll());

        if (resetBtn != null) {
            resetBtn.setOnAction(e -> {
                fromDate.setValue(defaultFrom);
                toDate.setValue(defaultTo);
                courseCombo.getSelectionModel().selectFirst();
                groupCombo.getSelectionModel().selectFirst();
                onTimeChk.setSelected(true);
                lateChk.setSelected(true);
                absentChk.setSelected(true);
                excusedChk.setSelected(true);
                refreshAll();
            });
        }
    }

    private void clampRangeAndRefresh() {
        if (fromDate.getValue()!=null && toDate.getValue()!=null &&
            toDate.getValue().isBefore(fromDate.getValue())) {
            toDate.setValue(fromDate.getValue());
        }
        refreshAll();
    }

    private void refreshAll() {
        LocalDate fFrom = fromDate!=null && fromDate.getValue()!=null ? fromDate.getValue() : defaultFrom;
        LocalDate fTo   = toDate  !=null && toDate.getValue()  !=null ? toDate.getValue()   : defaultTo;

        boolean showOn   = onTimeChk  == null || onTimeChk.isSelected();
        boolean showLate = lateChk    == null || lateChk.isSelected();
        boolean showAbs  = absentChk  == null || absentChk.isSelected();
        boolean showExc  = excusedChk == null || excusedChk.isSelected();

        setupWeeklyChart(fFrom, fTo);

        int days = (int) ChronoUnit.DAYS.between(fFrom, fTo) + 1;
        int on   = Math.max(0, days * 3);
        int lt   = Math.max(0, days * 1);
        int ab   = Math.max(0, days * 1);
        int ex   = Math.max(0, days * 0);
        setupStatusPie(showOn?on:0, showLate?lt:0, showAbs?ab:0, showExc?ex:0);

        int[] present = new int[]{8,5,6};
        int[] late    = new int[]{2,1,2};
        int[] absent  = new int[]{1,2,1};
        if (!showOn)   present = new int[]{0,0,0};
        if (!showLate) late    = new int[]{0,0,0};
        if (!showAbs)  absent  = new int[]{0,0,0};
        setupCourseChart(present, late, absent);

        setupRecentTableFiltered(showOn, showLate, showAbs, showExc);

        Platform.runLater(() -> {
            applyPalette(weeklyChart);
            applyPalette(arrivalHistogram);
            applyPalette(statusPie);
            applyPalette(courseChart);
            rebuildExternalPieLegend();
            rebuildExternalCourseLegend();
        });
    }

    /* ==================== Weekly chart ==================== */
    private void setupWeeklyChart(LocalDate from, LocalDate to) {
        chartXAxis.setLabel("Day");
        chartYAxis.setLabel("Attendance");
        chartYAxis.setMinorTickCount(0);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("This Range");

        long days = ChronoUnit.DAYS.between(from, to) + 1;
        Random rnd = new Random(42);
        for (int i = 0; i < days; i++) {
            LocalDate d = from.plusDays(i);
            String key = d.getDayOfWeek().toString().substring(0, 3);
            int value = 1 + rnd.nextInt(3);
            series.getData().add(new XYChart.Data<>(key, value));
        }
        weeklyChart.getData().setAll(series);
        addBarValueLabels(weeklyChart);
    }

    /* ==================== Recent activity ==================== */
    private void setupRecentTable() {
        colTime.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("time"));
        colStudent.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("student"));
        colStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        rebuildRecentItems(true, true, true, true);
    }

    private void setupRecentTableFiltered(boolean showOn, boolean showLate, boolean showAbs, boolean showExc) {
        rebuildRecentItems(showOn, showLate, showAbs, showExc);
    }

    private void rebuildRecentItems(boolean showOn, boolean showLate, boolean showAbs, boolean showExc) {
        List<ActivityItem> rows = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        List<Student> all = studentService.getAllStudents();
        int n = Math.min(6, all.size());
        for (int i = 0; i < n; i++) {
            Student s = all.get(i);
            String status;
            switch (i % 4) {
                case 0: status = "On-time"; break;
                case 1: status = "Late";    break;
                case 2: status = "Absent";  break;
                default: status = "Excused";
            }
            boolean keep = (status.equals("On-time") && showOn)
                        || (status.equals("Late") && showLate)
                        || (status.equals("Absent") && showAbs)
                        || (status.equals("Excused") && showExc);
            if (keep) {
                rows.add(new ActivityItem(
                    LocalDateTime.now().minusMinutes((i + 1) * 5).format(fmt),
                    s.getUserName(), status));
            }
        }
        recentTable.setItems(FXCollections.observableArrayList(rows));
    }

    public static class ActivityItem {
        private final String time;
        private final String student;
        private final String status;
        public ActivityItem(String time, String student, String status) {
            this.time = time; this.student = student; this.status = status;
        }
        public String getTime() { return time; }
        public String getStudent() { return student; }
        public String getStatus() { return status; }
    }

    /* ==================== Arrival histogram ==================== */
    private void setupArrivalHistogram() {
        int[] arrivals = new int[]{-2, 0, 1, 4, 7, 12, 18, 3, 6, 0, -1, 20};
        String[] bins = {"≤0", "0–5", "6–10", "11–15", ">15"};
        int[] counts = new int[bins.length];
        for (int m : arrivals) {
            int idx = (m <= 0) ? 0 : (m <= 5 ? 1 : (m <= 10 ? 2 : (m <= 15 ? 3 : 4)));
            counts[idx]++;
        }
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Arrival minutes");
        for (int i = 0; i < bins.length; i++) {
            s.getData().add(new XYChart.Data<>(bins[i], counts[i]));
        }

        arrivalXAxis.setLabel("Minutes vs start");
        arrivalYAxis.setLabel("Students");
        arrivalYAxis.setMinorTickCount(0);

        arrivalHistogram.getData().setAll(s);
        addBarValueLabels(arrivalHistogram);
    }

    /* ==================== Status pie (card) ==================== */
    private void setupStatusPie(int ontime, int late, int absent, int excused) {
        statusPie.getData().setAll(
            new PieChart.Data("On-time", ontime),
            new PieChart.Data("Late", late),
            new PieChart.Data("Absent", absent),
            new PieChart.Data("Excused", excused)
        );
        statusPie.setLabelsVisible(false);
        statusPie.setLegendVisible(false);      // built-in off in card

        Platform.runLater(() -> {
            applyPalette(statusPie);
            rebuildExternalPieLegend();
        });

        statusPie.getData().addListener((ListChangeListener<PieChart.Data>) c ->
            Platform.runLater(() -> {
                applyPalette(statusPie);
                rebuildExternalPieLegend();
            })
        );
    }

    /* ==================== Attendance by course (card) ==================== */
    private void setupCourseChart(int[] present, int[] late, int[] absent) {
        String[] courses = {"CS102", "MA101", "PHY110"};
        XYChart.Series<String, Number> sPresent = new XYChart.Series<>();
        sPresent.setName("On-time");
        XYChart.Series<String, Number> sLate = new XYChart.Series<>();
        sLate.setName("Late");
        XYChart.Series<String, Number> sAbsent = new XYChart.Series<>();
        sAbsent.setName("Absent");

        for (int i = 0; i < courses.length; i++) {
            sPresent.getData().add(new XYChart.Data<>(courses[i], present[i]));
            sLate.getData().add(new XYChart.Data<>(courses[i], late[i]));
            sAbsent.getData().add(new XYChart.Data<>(courses[i], absent[i]));
        }

        courseXAxis.setLabel("Course");
        courseYAxis.setLabel("Count");
        courseYAxis.setMinorTickCount(0);

        courseChart.getData().setAll(sPresent, sLate, sAbsent);
        courseChart.setLegendVisible(false);    // built-in off in card

        Platform.runLater(() -> {
            applyPalette(courseChart);
            rebuildExternalCourseLegend();
        });
    }

    /* ==================== Tooltip wiring ==================== */

    private void wireTooltips() {
        Platform.runLater(() -> {
            attachBarTooltips(weeklyChart, "This Range");
            attachBarTooltips(arrivalHistogram, "Arrival minutes");
            attachStackedBarTooltips(courseChart);
            attachPieTooltips(statusPie);

            addBarDataListeners(weeklyChart, "This Range");
            addBarDataListeners(arrivalHistogram, "Arrival minutes");
            addStackedBarDataListeners(courseChart);
            addPieDataListeners(statusPie);
        });
    }

    private void addBarDataListeners(BarChart<String, Number> chart, String fallback) {
        chart.getData().addListener((ListChangeListener<XYChart.Series<String, Number>>) c ->
            attachBarTooltips(chart, fallback));
        for (XYChart.Series<String, Number> s : chart.getData()) {
            s.getData().addListener((ListChangeListener<XYChart.Data<String, Number>>) c ->
                attachBarTooltips(chart, fallback));
        }
    }

    private void addStackedBarDataListeners(StackedBarChart<String, Number> chart) {
        chart.getData().addListener((ListChangeListener<XYChart.Series<String, Number>>) c ->
            attachStackedBarTooltips(chart));
        for (XYChart.Series<String, Number> s : chart.getData()) {
            s.getData().addListener((ListChangeListener<XYChart.Data<String, Number>>) c ->
                attachStackedBarTooltips(chart));
        }
    }

    private void addPieDataListeners(PieChart pie) {
        pie.getData().addListener((ListChangeListener<PieChart.Data>) c ->
            attachPieTooltips(pie));
    }

    private void attachBarTooltips(BarChart<String, Number> chart, String fallbackSeriesName) {
        for (XYChart.Series<String, Number> s : chart.getData()) {
            String sName = (s.getName() != null) ? s.getName() : fallbackSeriesName;
            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() != null) {
                    installTooltip(d.getNode(), sName + "\n" + d.getXValue() + ": " + d.getYValue());
                } else {
                    d.nodeProperty().addListener((obs, old, node) -> {
                        if (node != null) {
                            installTooltip(node, sName + "\n" + d.getXValue() + ": " + d.getYValue());
                        }
                    });
                }
            }
        }
    }

    private void attachStackedBarTooltips(StackedBarChart<String, Number> chart) {
        for (XYChart.Series<String, Number> s : chart.getData()) {
            String sName = (s.getName() != null) ? s.getName() : "";
            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() != null) {
                    installTooltip(d.getNode(), sName + "\n" + d.getXValue() + ": " + d.getYValue());
                } else {
                    d.nodeProperty().addListener((obs, old, node) -> {
                        if (node != null) {
                            installTooltip(node, sName + "\n" + d.getXValue() + ": " + d.getYValue());
                        }
                    });
                }
            }
        }
    }

    private void attachPieTooltips(PieChart pie) {
        double total = pie.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();
        for (PieChart.Data d : pie.getData()) {
            double pct = total > 0 ? (d.getPieValue() * 100.0 / total) : 0.0;
            String msg = String.format("%s%n%d (%.1f%%)", d.getName(), (int) d.getPieValue(), pct);
            if (d.getNode() != null) {
                installTooltip(d.getNode(), msg);
            } else {
                d.nodeProperty().addListener((obs, old, node) -> {
                    if (node != null) installTooltip(node, msg);
                });
            }
        }
    }

    private void installTooltip(Node node, String text) {
        Tooltip tip = new Tooltip(text);
        tip.getStyleClass().add("tooltip");
        tip.setShowDelay(Duration.millis(100));
        tip.setHideDelay(Duration.millis(50));
        tip.setShowDuration(Duration.seconds(30));
        Tooltip.install(node, tip);
    }

    /* ==================== Visual helpers ==================== */

    private void polishNumberAxis(NumberAxis axis) {
        axis.setMinorTickCount(0);
        axis.setTickMarkVisible(false);
        axis.setMinorTickVisible(false);
    }

    private void addBarValueLabels(BarChart<String, Number> chart) {
        for (XYChart.Series<String, Number> s : chart.getData()) {
            for (XYChart.Data<String, Number> d : s.getData()) {
                d.nodeProperty().addListener((obs, old, node) -> {
                    if (node != null) attachBarLabel(node, d);
                });
                if (d.getNode() != null) attachBarLabel(d.getNode(), d);
            }
        }
    }

    private void attachBarLabel(Node node, XYChart.Data<String, Number> d) {
        Label l = new Label(String.valueOf(d.getYValue()));
        l.getStyleClass().add("bar-label");
        node.parentProperty().addListener((obs, oldP, newP) -> {
            if (newP instanceof Group && !((Group)newP).getChildren().contains(l)) {
                ((Group)newP).getChildren().add(l);
            }
        });
        node.boundsInParentProperty().addListener((o, oldB, b) -> {
            l.relocate(
                Math.round(b.getMinX() + b.getWidth() / 2 - l.getWidth() / 2.0),
                Math.round(b.getMinY() - 16)
            );
        });
    }

    /* ==================== ZOOM via CLONED CHARTS ==================== */

    @FXML private void zoomWeekly()  { openZoomWindowWithClone(weeklyChart,  "Attendance (This Week)"); }
    @FXML private void zoomArrival() { openZoomWindowWithClone(arrivalHistogram, "Arrival Time Distribution"); }
    @FXML private void zoomPie()     { openZoomWindowWithClone(statusPie,   "Today’s Status Split"); }
    @FXML private void zoomCourse()  { openZoomWindowWithClone(courseChart, "Attendance by Course"); }

    private void openZoomWindowWithClone(Node chart, String title) {
        if (chart == null) return;

        Node clone = cloneChart(chart);
        if (clone == null) return;

        BorderPane root = new BorderPane(clone);
        root.setPadding(new Insets(12));

        Label heading = new Label(title);
        heading.getStyleClass().add("section-title");
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, heading, spacer, closeBtn);
        header.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(header);

        Scene zoomScene = new Scene(root, 900, 600);
        Scene originScene = chart.getScene();
        if (originScene != null) {
            zoomScene.getStylesheets().addAll(originScene.getStylesheets());
            if (originScene.getRoot() != null) {
                root.getStyleClass().addAll(originScene.getRoot().getStyleClass());
            }
        }
        if (!root.getStyleClass().contains("dashboard")) root.getStyleClass().add("dashboard");

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setScene(zoomScene);

        stage.setOnShown(ev -> Platform.runLater(() -> {
            clone.applyCss();
            if (clone instanceof Parent) ((Parent) clone).layout();
            // In the zoom window we *want* native legends:
            if (clone instanceof PieChart) ((PieChart) clone).setLegendVisible(true);
            if (clone instanceof StackedBarChart) ((StackedBarChart<?, ?>) clone).setLegendVisible(true);
            applyPalette(clone);
            if (clone instanceof Chart) forceLegendVisible((Chart) clone);
        }));

        stage.show();
    }

    private Node cloneChart(Node chart) {
        if (chart instanceof PieChart) {
            PieChart src = (PieChart) chart;
            PieChart dst = new PieChart();
            for (PieChart.Data d : src.getData()) {
                dst.getData().add(new PieChart.Data(d.getName(), d.getPieValue()));
            }
            dst.setLabelsVisible(src.getLabelsVisible());
            dst.setLegendSide(Side.TOP);
            dst.setLegendVisible(true); // ensure legend in zoom
            dst.setStartAngle(src.getStartAngle());
            return dst;
        }

        if (chart instanceof BarChart<?, ?>) {
            @SuppressWarnings("unchecked")
            BarChart<String, Number> src = (BarChart<String, Number>) chart;
            CategoryAxis x = new CategoryAxis();
            x.setLabel(src.getXAxis().getLabel());
            NumberAxis y = new NumberAxis();
            y.setLabel(src.getYAxis().getLabel());
            BarChart<String, Number> dst = new BarChart<>(x, y);
            dst.setCategoryGap(src.getCategoryGap());
            dst.setBarGap(src.getBarGap());
            dst.setLegendVisible(src.isLegendVisible());
            dst.setAnimated(false);
            for (XYChart.Series<String, Number> s : src.getData()) {
                XYChart.Series<String, Number> copy = new XYChart.Series<>();
                copy.setName(s.getName());
                for (XYChart.Data<String, Number> d : s.getData()) {
                    copy.getData().add(new XYChart.Data<>(d.getXValue(), d.getYValue()));
                }
                dst.getData().add(copy);
            }
            return dst;
        }

        if (chart instanceof StackedBarChart<?, ?>) {
            @SuppressWarnings("unchecked")
            StackedBarChart<String, Number> src = (StackedBarChart<String, Number>) chart;
            CategoryAxis x = new CategoryAxis();
            x.setLabel(src.getXAxis().getLabel());
            NumberAxis y = new NumberAxis();
            y.setLabel(src.getYAxis().getLabel());
            StackedBarChart<String, Number> dst = new StackedBarChart<>(x, y);
            dst.setLegendSide(Side.TOP);
            dst.setLegendVisible(true); // ensure legend in zoom
            dst.setAnimated(false);
            for (XYChart.Series<String, Number> s : src.getData()) {
                XYChart.Series<String, Number> copy = new XYChart.Series<>();
                copy.setName(s.getName());
                for (XYChart.Data<String, Number> d : s.getData()) {
                    copy.getData().add(new XYChart.Data<>(d.getXValue(), d.getYValue()));
                }
                dst.getData().add(copy);
            }
            return dst;
        }

        return null;
    }

    /* ==================== Palette forcing ==================== */

    private void applyPalette(Node chart) {
        if (chart == null) return;

        chart.applyCss();
        if (chart instanceof Parent) ((Parent) chart).layout();

        if (chart instanceof PieChart) {
            PieChart pie = (PieChart) chart;
            List<PieChart.Data> data = pie.getData();
            Map<String, String> nameToColor = new HashMap<>();
            for (int i = 0; i < data.size(); i++) {
                String c = SERIES_COLORS[i % SERIES_COLORS.length];
                PieChart.Data d = data.get(i);
                nameToColor.put(d.getName(), c);
                if (d.getNode() != null) d.getNode().setStyle("-fx-pie-color: " + c + ";");
            }
            colorLegendByName(pie, nameToColor);
            return;
        }

        if (chart instanceof BarChart<?, ?>) {
            @SuppressWarnings("unchecked")
            BarChart<?, Number> bc = (BarChart<?, Number>) chart;
            @SuppressWarnings("unchecked")
            List<XYChart.Series<?, Number>> seriesList =
                (List<XYChart.Series<?, Number>>) (List<?>) bc.getData();
            Map<String, String> nameToColor = new HashMap<>();
            for (int si = 0; si < seriesList.size(); si++) {
                String c = SERIES_COLORS[si % SERIES_COLORS.length];
                XYChart.Series<?, Number> series = seriesList.get(si);
                if (series.getName() != null) nameToColor.put(series.getName(), c);
                for (XYChart.Data<?, Number> d : series.getData()) {
                    Node n = d.getNode();
                    if (n != null) n.setStyle("-fx-bar-fill: " + c + ";");
                }
            }
            colorLegendByName(bc, nameToColor);
            return;
        }

        if (chart instanceof StackedBarChart<?, ?>) {
            @SuppressWarnings("unchecked")
            StackedBarChart<?, Number> sbc = (StackedBarChart<?, Number>) chart;
            @SuppressWarnings("unchecked")
            List<XYChart.Series<?, Number>> seriesList =
                (List<XYChart.Series<?, Number>>) (List<?>) sbc.getData();
            Map<String, String> nameToColor = new HashMap<>();
            for (int si = 0; si < seriesList.size(); si++) {
                String c = SERIES_COLORS[si % SERIES_COLORS.length];
                XYChart.Series<?, Number> series = seriesList.get(si);
                if (series.getName() != null) nameToColor.put(series.getName(), c);
                for (XYChart.Data<?, Number> d : series.getData()) {
                    Node n = d.getNode();
                    if (n != null) n.setStyle("-fx-bar-fill: " + c + ";");
                }
            }
            colorLegendByName(sbc, nameToColor);
        }
    }

    private void colorLegendByName(Node chart, Map<String, String> nameToColor) {
        if (chart == null || nameToColor.isEmpty()) return;

        Node legend = chart.lookup(".chart-legend");
        if (!(legend instanceof Parent)) return;
        Parent legendParent = (Parent) legend;

        for (Node item : legendParent.getChildrenUnmodifiable()) {
            if (!(item instanceof Parent)) continue;
            Parent itemParent = (Parent) item;
            Node labelNode = itemParent.lookup(".label");
            Node symbolNode = itemParent.lookup(".chart-legend-item-symbol");
            if (labelNode instanceof Label && symbolNode instanceof Region) {
                Label lbl = (Label) labelNode;
                Region sym = (Region) symbolNode;
                String color = nameToColor.get(lbl.getText());
                if (color != null) {
                    sym.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");
                }
            }
        }
    }

    private void forceLegendVisible(Chart chart) {
        if (chart == null) return;
        chart.setLegendVisible(true);
        chart.applyCss();
        if (chart instanceof Parent) ((Parent) chart).layout();
        Node legend = chart.lookup(".chart-legend");
        if (legend != null) {
            legend.setVisible(true);
            legend.setManaged(true);
        }
    }

    /* ==================== External legends (rows above charts) ==================== */

    private void rebuildExternalPieLegend() {
        if (statusLegendBox == null || statusPie == null) return;
        statusLegendBox.getChildren().clear();
        List<PieChart.Data> data = statusPie.getData();
        for (int i = 0; i < data.size(); i++) {
            statusLegendBox.getChildren().add(makeLegendItem(
                data.get(i).getName(), SERIES_COLORS[i % SERIES_COLORS.length]));
        }
    }

    private void rebuildExternalCourseLegend() {
        if (courseLegendBox == null || courseChart == null) return;
        courseLegendBox.getChildren().clear();
        List<XYChart.Series<String, Number>> seriesList = courseChart.getData();
        for (int i = 0; i < seriesList.size(); i++) {
            XYChart.Series<String, Number> s = seriesList.get(i);
            String name = (s.getName() == null || s.getName().isEmpty())
                          ? ("Series " + (i + 1)) : s.getName();
            courseLegendBox.getChildren().add(makeLegendItem(
                name, SERIES_COLORS[i % SERIES_COLORS.length]));
        }
    }

    private HBox makeLegendItem(String name, String colorHex) {
        Region swatch = new Region();
        swatch.setMinSize(12, 12);
        swatch.setPrefSize(12, 12);
        swatch.setMaxSize(12, 12);
        swatch.setStyle("-fx-background-radius: 6; -fx-background-color: " + colorHex + ";");

        Label lbl = new Label(name);
        lbl.setStyle("-fx-font-weight: 600; -fx-padding: 0 8 0 6;");

        HBox box = new HBox(6, swatch, lbl);
        box.getStyleClass().add("legend-row");
        box.setPadding(new Insets(2, 6, 2, 2));
        return box;
    }

    /* ==================== PNG EXPORT ==================== */

    @FXML private void exportWeeklyPng()  { exportChartWithLegend(weeklyChart,  "weekly-attendance", false); }
    @FXML private void exportArrivalPng() { exportChartWithLegend(arrivalHistogram, "arrival-distribution", false); }
    @FXML private void exportStatusPng()  { exportChartWithLegend(statusPie,   "status-split", true); }
    @FXML private void exportCoursePng()  { exportChartWithLegend(courseChart, "attendance-by-course", true); }

    /**
     * Export by cloning the chart into an off-screen Scene, optionally
     * enabling the native legend, then snapshotting the wrapper.
     */
    private void exportChartWithLegend(Node chartNode, String baseName, boolean wantLegend) {
        if (chartNode == null) return;

        // 1) Clone the chart so we don't disturb the live UI
        Node clone = cloneChart(chartNode);
        if (clone == null) return;

        // 2) Force legend for the clone when requested
        if (wantLegend && clone instanceof Chart) {
            if (clone instanceof PieChart) {
                ((PieChart) clone).setLegendSide(Side.TOP);
                ((PieChart) clone).setLegendVisible(true);
            } else if (clone instanceof StackedBarChart) {
                ((StackedBarChart<?, ?>) clone).setLegendSide(Side.TOP);
                ((StackedBarChart<?, ?>) clone).setLegendVisible(true);
            } else {
                ((Chart) clone).setLegendVisible(true);
            }
        }

        // 3) Wrap clone in a styled container
        BorderPane wrapper = new BorderPane(clone);
        wrapper.setPadding(new Insets(16));
        wrapper.setStyle("-fx-background-color: white;");

        // 4) Try to match on-screen size
        double w = Math.max(800, Math.round(
                (chartNode.getBoundsInParent() != null ? chartNode.getBoundsInParent().getWidth() : 1000)));
        double h = Math.max(460, Math.round(
                (chartNode.getBoundsInParent() != null ? chartNode.getBoundsInParent().getHeight() : 520)
                + (wantLegend ? 80 : 0)));

        wrapper.setPrefSize(w + 32, h + 32);
        if (clone instanceof Region) ((Region) clone).setPrefSize(w, h);

        // 5) Off-screen Scene so charts fully realize their nodes
        Scene liveScene = chartNode.getScene();
        Scene offscreen = new Scene(wrapper, w + 32, h + 32);
        if (liveScene != null) {
            offscreen.getStylesheets().addAll(liveScene.getStylesheets());
            if (liveScene.getRoot() != null) wrapper.getStyleClass().addAll(liveScene.getRoot().getStyleClass());
        }
        if (!wrapper.getStyleClass().contains("dashboard")) wrapper.getStyleClass().add("dashboard");

        // 6) CSS + layout + palette
        offscreen.getRoot().applyCss();
        if (offscreen.getRoot() instanceof Parent) ((Parent) offscreen.getRoot()).layout();
        applyPalette(clone);
        if (wantLegend && clone instanceof Chart) forceLegendVisible((Chart) clone);

        // 7) Snapshot the off-screen Scene
        WritableImage image = offscreen.snapshot(null);

        // 8) Save
        FileChooser fc = new FileChooser();
        fc.setTitle("Export chart as PNG");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        fc.setInitialFileName(baseName + ".png");
        File file = (liveScene != null) ? fc.showSaveDialog(liveScene.getWindow())
                                        : fc.showSaveDialog(null);
        if (file == null) return;

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
