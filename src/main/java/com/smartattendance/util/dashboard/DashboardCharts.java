package com.smartattendance.util.dashboard;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.smartattendance.model.dto.dashboard.AttendanceRecord;
import com.smartattendance.util.chart.ChartStyler;
import com.smartattendance.util.chart.DefaultChartStyler;
import com.smartattendance.util.chart.DefaultLegendBuilder;
import com.smartattendance.util.chart.LegendBuilder;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * Builds dashboard charts from raw attendance records.
 * Uses util.chart classes.
 */
public final class DashboardCharts {

    private static final ChartStyler   STYLER  = new DefaultChartStyler();
    private static final LegendBuilder LEGENDS = new DefaultLegendBuilder();

    private DashboardCharts() {}

    /* ================= WEEKLY ================= */
    public static void setupWeeklyChart(BarChart<String, Number> chart,
                                        CategoryAxis x,
                                        NumberAxis y,
                                        List<AttendanceRecord> records,
                                        LocalDate from,
                                        LocalDate to) {
        if (chart == null || from == null || to == null) return;

        long days = ChronoUnit.DAYS.between(from, to) + 1;
        Map<LocalDate,Integer> perDay = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            perDay.put(from.plusDays(i), 0);
        }

        for (AttendanceRecord r : records) {
            if (r.markedAt == null) continue;
            LocalDate d = r.markedAt.toLocalDate();
            if (!d.isBefore(from) && !d.isAfter(to)) {
                perDay.put(d, perDay.get(d) + 1);
            }
        }

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (Map.Entry<LocalDate,Integer> e : perDay.entrySet()) {
            String label = e.getKey().getDayOfWeek().toString().substring(0,3);
            s.getData().add(new XYChart.Data<>(label, e.getValue()));
        }
        chart.getData().setAll(s);

        STYLER.styleBarChart(chart);
        addBarTooltips(chart, "Attendance");
    }

    /* ================= ARRIVAL ================= */
    public static void setupArrivalHistogram(BarChart<String, Number> chart,
                                             CategoryAxis x,
                                             NumberAxis y,
                                             List<AttendanceRecord> records) {
        if (chart == null) return;

        String[] bins = {"≤0", "1–5", "6–10", "11–15", ">15"};
        int[] counts  = new int[bins.length];

        for (AttendanceRecord r : records) {
            if (r.markedAt == null || r.sessionStart == null) continue;
            long mins = java.time.Duration.between(r.sessionStart, r.markedAt).toMinutes();
            int idx;
            if (mins <= 0) idx = 0;
            else if (mins <= 5) idx = 1;
            else if (mins <= 10) idx = 2;
            else if (mins <= 15) idx = 3;
            else idx = 4;
            counts[idx]++;
        }

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (int i = 0; i < bins.length; i++) {
            s.getData().add(new XYChart.Data<>(bins[i], counts[i]));
        }
        chart.getData().setAll(s);

        STYLER.styleBarChart(chart);
        addBarTooltips(chart, "Arrivals");
    }

    /* ================= STATUS PIE ================= */
    public static void setupStatusPie(PieChart pie,
                                      HBox legendBox,
                                      List<AttendanceRecord> records,
                                      boolean showPresent,
                                      boolean showLate,
                                      boolean showAbsent,
                                      boolean showPending) {
        if (pie == null) return;

        int present = 0, late = 0, absent = 0, pending = 0;
        for (AttendanceRecord r : records) {
            String st = (r.status == null || r.status.isEmpty()) ? "Pending" : r.status;
            if ("Present".equals(st)) present++;
            else if ("Late".equals(st)) late++;
            else if ("Absent".equals(st)) absent++;
            else pending++;
        }

        pie.getData().clear();
        if (showPresent) pie.getData().add(new PieChart.Data("Present", present));
        if (showLate)    pie.getData().add(new PieChart.Data("Late", late));
        if (showAbsent)  pie.getData().add(new PieChart.Data("Absent", absent));
        if (showPending) pie.getData().add(new PieChart.Data("Pending", pending));

        STYLER.stylePieChart(pie);
        addPieTooltips(pie);

        if (legendBox != null) {
            legendBox.getChildren().setAll(
                    LEGENDS.buildLegendNodes(pie.getData())
            );
        }
    }

    /* ================= COURSE STACKED ================= */
    public static void setupCourseChart(StackedBarChart<String, Number> chart,
                                        CategoryAxis x,
                                        NumberAxis y,
                                        HBox legendBox,
                                        List<AttendanceRecord> records,
                                        boolean showPresent,
                                        boolean showLate,
                                        boolean showAbsent,
                                        boolean showPending) {
        if (chart == null) return;

        Map<String, int[]> perCourse = new LinkedHashMap<>();
        for (AttendanceRecord r : records) {
            String course = (r.courseCode != null && !r.courseCode.isBlank())
                    ? r.courseCode : "Unknown";
            perCourse.putIfAbsent(course, new int[4]);
            int[] arr = perCourse.get(course);
            String st = (r.status == null || r.status.isEmpty()) ? "Pending" : r.status;
            if ("Present".equals(st)) arr[0]++;
            else if ("Late".equals(st)) arr[1]++;
            else if ("Absent".equals(st)) arr[2]++;
            else arr[3]++;
        }

        if (perCourse.isEmpty()) {
            chart.getData().clear();
            return;
        }

        List<String> courses = new ArrayList<>(perCourse.keySet());
        x.setCategories(FXCollections.observableArrayList(courses));

        List<XYChart.Series<String, Number>> seriesList = new ArrayList<>();

        if (showPresent) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName("Present");
            for (String c : courses) s.getData().add(new XYChart.Data<>(c, perCourse.get(c)[0]));
            seriesList.add(s);
        }
        if (showLate) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName("Late");
            for (String c : courses) s.getData().add(new XYChart.Data<>(c, perCourse.get(c)[1]));
            seriesList.add(s);
        }
        if (showAbsent) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName("Absent");
            for (String c : courses) s.getData().add(new XYChart.Data<>(c, perCourse.get(c)[2]));
            seriesList.add(s);
        }
        if (showPending) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName("Pending");
            for (String c : courses) s.getData().add(new XYChart.Data<>(c, perCourse.get(c)[3]));
            seriesList.add(s);
        }

        chart.getData().setAll(seriesList);
        STYLER.styleStackedBarChart(chart);
        addStackedTooltips(chart);

        if (legendBox != null) {
            legendBox.getChildren().setAll(
                    LEGENDS.buildLegendNodesFromSeries(seriesList)
            );
        }
    }

    /* ================= TOOLTIP HELPERS ================= */
    private static void addBarTooltips(BarChart<String, Number> chart, String fallback) {
        for (XYChart.Series<String, Number> s : chart.getData()) {
            String name = (s.getName() != null) ? s.getName() : fallback;
            for (XYChart.Data<String, Number> d : s.getData()) {
                Node n = d.getNode();
                if (n == null) continue;
                Tooltip.install(n, new Tooltip(name + "\n" + d.getXValue() + ": " + d.getYValue()));
            }
        }
    }

    private static void addPieTooltips(PieChart pie) {
        double total = 0;
        for (PieChart.Data d : pie.getData()) total += d.getPieValue();
        for (PieChart.Data d : pie.getData()) {
            Node n = d.getNode();
            if (n == null) continue;
            double pct = total > 0 ? (d.getPieValue() * 100.0 / total) : 0.0;
            String txt = String.format("%s%n%d (%.1f%%)", d.getName(), (int)d.getPieValue(), pct);
            Tooltip.install(n, new Tooltip(txt));
        }
    }

    private static void addStackedTooltips(StackedBarChart<String, Number> chart) {
        for (XYChart.Series<String, Number> s : chart.getData()) {
            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() == null) continue;
                Tooltip.install(d.getNode(),
                        new Tooltip(s.getName() + "\n" + d.getXValue() + ": " + d.getYValue()));
            }
        }
    }
}
