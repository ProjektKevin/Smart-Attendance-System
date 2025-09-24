package com.smartattendance.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.smartattendance.model.Student;
import com.smartattendance.service.StudentService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Dashboard controller with placeholder data.
 * Wire these methods to SQLite later (counts, joins, GROUP BY, etc.).
 */
public class DashboardController {

    // Top metric cards
    @FXML
    private Label studentsCount;
    @FXML
    private Label sessionsCount;
    @FXML
    private Label presentTodayCount;

    // Weekly attendance chart
    @FXML
    private BarChart<String, Number> weeklyChart;
    @FXML
    private CategoryAxis chartXAxis;
    @FXML
    private NumberAxis chartYAxis;

    // Recent activity table
    @FXML
    private TableView<ActivityItem> recentTable;
    @FXML
    private TableColumn<ActivityItem, String> colTime;
    @FXML
    private TableColumn<ActivityItem, String> colStudent;
    @FXML
    private TableColumn<ActivityItem, String> colStatus;

    // New charts
    @FXML
    private BarChart<String, Number> arrivalHistogram;
    @FXML
    private CategoryAxis arrivalXAxis;
    @FXML
    private NumberAxis arrivalYAxis;

    @FXML
    private PieChart statusPie;

    @FXML
    private StackedBarChart<String, Number> courseChart;
    @FXML
    private CategoryAxis courseXAxis;
    @FXML
    private NumberAxis courseYAxis;

    @FXML
    private ScatterChart<Number, Number> regressChart;
    @FXML
    private NumberAxis regXAxis;
    @FXML
    private NumberAxis regYAxis;

    private final StudentService studentService = new StudentService();

    @FXML
    public void initialize() {
        // Top cards (placeholder)
        int totalStudents = studentService.getAllStudents().size();
        studentsCount.setText(String.valueOf(totalStudents));
        int stubSessions = 3;
        int stubPresent = Math.min(totalStudents, 2);
        sessionsCount.setText(String.valueOf(stubSessions));
        presentTodayCount.setText(String.valueOf(stubPresent));

        setupWeeklyChart();
        setupRecentTable();

        // New visuals
        setupArrivalHistogram();
        setupStatusPie();
        setupCourseChart();
        setupRegressionConfidenceVsArrival();
    }

    /* -------------------- Weekly chart -------------------- */
    private void setupWeeklyChart() {
        chartXAxis.setLabel("Day");
        chartYAxis.setLabel("Attendance");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("This Week");

        LocalDate today = LocalDate.now();
        Random rnd = new Random(42);
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String key = d.getDayOfWeek().toString().substring(0, 3);
            int value = 1 + rnd.nextInt(3); // 1..3 present (placeholder)
            series.getData().add(new XYChart.Data<>(key, value));
        }
        weeklyChart.getData().setAll(series);
        weeklyChart.setAnimated(false);
    }

    /* -------------------- Recent activity -------------------- */
    private void setupRecentTable() {
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colStudent.setCellValueFactory(new PropertyValueFactory<>("student"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        List<ActivityItem> rows = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

        List<Student> all = studentService.getAllStudents();
        int n = Math.min(3, all.size());
        for (int i = 0; i < n; i++) {
            Student s = all.get(i);
            rows.add(new ActivityItem(
                    LocalDateTime.now().minusMinutes((i + 1) * 5).format(fmt),
                    s.getName(),
                    (i == 0 ? "Present" : "Late")));
        }
        ObservableList<ActivityItem> data = FXCollections.observableArrayList(rows);
        recentTable.setItems(data);
    }

    public static class ActivityItem {
        private final String time;
        private final String student;
        private final String status;

        public ActivityItem(String time, String student, String status) {
            this.time = time;
            this.student = student;
            this.status = status;
        }

        public String getTime() {
            return time;
        }

        public String getStudent() {
            return student;
        }

        public String getStatus() {
            return status;
        }
    }

    /* -------------------- Arrival histogram -------------------- */
    private void setupArrivalHistogram() {
        // Placeholder arrival offsets (minutes relative to session start; negative =
        // early)
        int[] arrivals = new int[] { -2, 0, 1, 4, 7, 12, 18, 3, 6, 0, -1, 20 };
        String[] bins = { "≤0", "0–5", "6–10", "11–15", ">15" };
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
        arrivalHistogram.getData().setAll(s);
        arrivalHistogram.setLegendVisible(false);
        arrivalHistogram.setAnimated(false);
    }

    /* -------------------- Status pie -------------------- */
    private void setupStatusPie() {
        // Placeholder: replace with counts from attendance table later
        int ontime = 7, late = 3, absent = 2, excused = 0;
        statusPie.getData().setAll(
                new PieChart.Data("On-time", ontime),
                new PieChart.Data("Late", late),
                new PieChart.Data("Absent", absent),
                new PieChart.Data("Excused", excused));
        statusPie.setLabelsVisible(true);
        statusPie.setLegendVisible(true);
    }

    /* -------------------- Attendance by course -------------------- */
    private void setupCourseChart() {
        String[] courses = { "CS102", "MA101", "PHY110" };
        int[] present = { 8, 5, 6 };
        int[] late = { 2, 1, 2 };
        int[] absent = { 1, 2, 1 };

        XYChart.Series<String, Number> sPresent = new XYChart.Series<>();
        sPresent.setName("Present");
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
        courseChart.getData().setAll(sPresent, sLate, sAbsent);
        courseChart.setAnimated(false);
    }

    /*
     * -------------------- Regression: confidence vs arrival Δ --------------------
     */
    private void setupRegressionConfidenceVsArrival() {
        // Placeholder data: X = recognition confidence (0..1), Y = arrival delta
        // minutes (− early, + late)
        List<Double> x = Arrays.asList(0.72, 0.81, 0.90, 0.66, 0.78, 0.88, 0.95, 0.60, 0.84, 0.70);
        List<Double> y = Arrays.asList(-2.0, 0.0, 1.0, 8.0, 3.0, 2.0, 0.0, 12.0, 4.0, 6.0);

        // Scatter points
        XYChart.Series<Number, Number> points = new XYChart.Series<>();
        points.setName("Samples");
        for (int i = 0; i < x.size(); i++) {
            points.getData().add(new XYChart.Data<>(x.get(i), y.get(i)));
        }

        // Linear regression y = a + b x
        double[] ab = linReg(x, y);
        double a = ab[0], b = ab[1];

        double minX = Collections.min(x);
        double maxX = Collections.max(x);

        XYChart.Series<Number, Number> line = new XYChart.Series<>();
        line.setName(String.format("y = %.2fx %+.2f", b, a));
        line.getData().add(new XYChart.Data<>(minX, a + b * minX));
        line.getData().add(new XYChart.Data<>(maxX, a + b * maxX));

        regXAxis.setLabel("Recognition confidence");
        regYAxis.setLabel("Arrival Δ (minutes)");
        regressChart.getData().setAll(points, line);
        regressChart.setLegendVisible(true);
        regressChart.setAnimated(false);
    }

    /** Ordinary least squares for y = a + b x. Returns [a, b]. */
    private double[] linReg(List<Double> x, List<Double> y) {
        int n = x.size();
        double sx = 0, sy = 0, sxx = 0, sxy = 0;
        for (int i = 0; i < n; i++) {
            double xi = x.get(i), yi = y.get(i);
            sx += xi;
            sy += yi;
            sxx += xi * xi;
            sxy += xi * yi;
        }
        double denom = n * sxx - sx * sx;
        double b = (n * sxy - sx * sy) / denom;
        double a = (sy - b * sx) / n;
        return new double[] { a, b };
    }
}
