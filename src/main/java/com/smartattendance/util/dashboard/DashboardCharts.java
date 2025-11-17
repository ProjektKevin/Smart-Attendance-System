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
 * Utility class that builds and configures dashboard charts from
 * raw {@link AttendanceRecord} data.
 *
 * <p>Responsibilities:
 * <ul>
 *     <li>Aggregate raw attendance records into chart-friendly data structures</li>
 *     <li>Populate JavaFX charts (bar, stacked bar, pie) with those aggregates</li>
 *     <li>Apply shared styling and custom legends using the util.chart layer</li>
 *     <li>Add tooltips for better readability and quick inspection of values</li>
 * </ul>
 *
 * <p>All methods are static and side-effect the charts passed in, which makes
 * this class easy to call from controllers without needing to instantiate it.
 * 
 * @author Ernest Lun
 */
public final class DashboardCharts {

    /**
     * Shared {@link ChartStyler} used to apply a consistent visual theme to charts.
     */
    private static final ChartStyler STYLER = new DefaultChartStyler();

    /**
     * Shared {@link LegendBuilder} used to generate custom legend nodes.
     */
    private static final LegendBuilder LEGENDS = new DefaultLegendBuilder();

    /**
     * Private constructor to prevent instantiation.
     * This is a pure utility class.
     */
    private DashboardCharts() {}

    /* ================= WEEKLY ================= */

    /**
     * Build and configure a weekly attendance bar chart.
     *
     * <p>Behaviour:
     * <ul>
     *     <li>Initialises the X-axis with one bar per day in the {@code [from, to]} date range</li>
     *     <li>Counts how many records fall on each day (based on {@code markedAt})</li>
     *     <li>Labels each bar using the three-letter day-of-week (e.g. MON, TUE)</li>
     *     <li>Applies the shared chart styling and installs tooltips</li>
     * </ul>
     *
     * @param chart   target bar chart to populate (must have String X and Number Y)
     * @param x       category axis representing days of the week (not modified directly here)
     * @param y       numeric axis representing counts (styled via {@link ChartStyler})
     * @param records list of attendance records to aggregate
     * @param from    start date (inclusive) of the range; if {@code null}, the method returns
     * @param to      end date (inclusive) of the range; if {@code null}, the method returns
     */
    public static void setupWeeklyChart(BarChart<String, Number> chart,
                                        CategoryAxis x,
                                        NumberAxis y,
                                        List<AttendanceRecord> records,
                                        LocalDate from,
                                        LocalDate to) {
        // If chart or date range is missing, do nothing.
        if (chart == null || from == null || to == null) return;

        // Pre-populate a map with one entry per day in the range so that
        // even days with zero attendance get a bar with value 0.
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        Map<LocalDate, Integer> perDay = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            perDay.put(from.plusDays(i), 0);
        }

        // Count how many records fall on each date in the range.
        for (AttendanceRecord r : records) {
            if (r.markedAt == null) continue;
            LocalDate d = r.markedAt.toLocalDate();
            if (!d.isBefore(from) && !d.isAfter(to)) {
                perDay.put(d, perDay.get(d) + 1);
            }
        }

        // Convert the aggregated map into a single bar chart series.
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (Map.Entry<LocalDate, Integer> e : perDay.entrySet()) {
            String label = e.getKey().getDayOfWeek().toString().substring(0, 3);
            s.getData().add(new XYChart.Data<>(label, e.getValue()));
        }
        chart.getData().setAll(s);

        // Apply common styling and value tooltips.
        STYLER.styleBarChart(chart);
        addBarTooltips(chart, "Attendance");
    }

    /* ================= ARRIVAL ================= */

    /**
     * Build a histogram-like bar chart of arrival times relative to session start.
     *
     * <p>Behaviour:
     * <ul>
     *     <li>Defines discrete time bins: ≤0, 1–5, 6–10, 11–15, &gt;15 minutes</li>
     *     <li>Computes the difference between {@code sessionStart} and {@code markedAt}</li>
     *     <li>Increments the count of the appropriate bin for each valid record</li>
     *     <li>Applies the shared chart styling and installs tooltips</li>
     * </ul>
     *
     * @param chart   target bar chart to populate
     * @param x       category axis representing time bins
     * @param y       numeric axis representing counts
     * @param records list of attendance records to analyze
     */
    public static void setupArrivalHistogram(BarChart<String, Number> chart,
                                             CategoryAxis x,
                                             NumberAxis y,
                                             List<AttendanceRecord> records) {
        if (chart == null) return;

        // Define logical bins for minutes difference between session start and arrival.
        String[] bins = {"≤0", "1–5", "6–10", "11–15", ">15"};
        int[] counts  = new int[bins.length];

        // For each record, compute the delay in minutes and map it into a bin index.
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

        // Populate a single series with bin labels and counts.
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (int i = 0; i < bins.length; i++) {
            s.getData().add(new XYChart.Data<>(bins[i], counts[i]));
        }
        chart.getData().setAll(s);

        STYLER.styleBarChart(chart);
        addBarTooltips(chart, "Arrivals");
    }

    /* ================= STATUS PIE ================= */

    /**
     * Build a status distribution pie chart and optional custom legend.
     *
     * <p>Behaviour:
     * <ul>
     *     <li>Counts Present/Late/Absent/Pending from {@link AttendanceRecord#status}</li>
     *     <li>Treats {@code null} or empty status as {@code "Pending"}</li>
     *     <li>Filters which slices to show based on the boolean flags</li>
     *     <li>Applies a shared pie style, adds percentage tooltips</li>
     *     <li>Optionally renders a custom legend in {@code legendBox}</li>
     * </ul>
     *
     * @param pie         target pie chart to populate
     * @param legendBox   container in which to render the custom legend; may be {@code null}
     * @param records     list of attendance records
     * @param showPresent whether to include the "Present" slice
     * @param showLate    whether to include the "Late" slice
     * @param showAbsent  whether to include the "Absent" slice
     * @param showPending whether to include the "Pending" slice
     */
    public static void setupStatusPie(PieChart pie,
                                      HBox legendBox,
                                      List<AttendanceRecord> records,
                                      boolean showPresent,
                                      boolean showLate,
                                      boolean showAbsent,
                                      boolean showPending) {
        if (pie == null) return;

        // Aggregate counts per status. Unknown/empty statuses fall back to "Pending".
        int present = 0, late = 0, absent = 0, pending = 0;
        for (AttendanceRecord r : records) {
            String st = (r.status == null || r.status.isEmpty()) ? "Pending" : r.status;
            if ("Present".equals(st)) present++;
            else if ("Late".equals(st)) late++;
            else if ("Absent".equals(st)) absent++;
            else pending++;
        }

        // Build pie slices conditionally, depending on user-selected filters.
        pie.getData().clear();
        if (showPresent) pie.getData().add(new PieChart.Data("Present", present));
        if (showLate)    pie.getData().add(new PieChart.Data("Late", late));
        if (showAbsent)  pie.getData().add(new PieChart.Data("Absent", absent));
        if (showPending) pie.getData().add(new PieChart.Data("Pending", pending));

        STYLER.stylePieChart(pie);
        addPieTooltips(pie);

        // If a legend container is provided, build a custom legend using util.chart.
        if (legendBox != null) {
            legendBox.getChildren().setAll(
                    LEGENDS.buildLegendNodes(pie.getData())
            );
        }
    }

    /* ================= COURSE STACKED ================= */

    /**
     * Build a stacked bar chart of attendance status per course, with an optional custom legend.
     *
     * <p>Behaviour:
     * <ul>
     *     <li>Groups records by course code (falling back to "Unknown" if missing/blank)</li>
     *     <li>For each course, counts Present/Late/Absent/Pending occurrences</li>
     *     <li>Creates up to four stacked series, one per status, controlled by flags</li>
     *     <li>Sets X-axis categories in course order to keep the mapping stable</li>
     *     <li>Applies shared styling and installs stacked bar tooltips</li>
     *     <li>Optionally builds a custom legend in {@code legendBox}</li>
     * </ul>
     *
     * @param chart       target stacked bar chart
     * @param x           category axis representing courses
     * @param y           numeric axis representing counts
     * @param legendBox   optional container for the custom legend; may be {@code null}
     * @param records     list of attendance records
     * @param showPresent whether to include "Present" as a stacked series
     * @param showLate    whether to include "Late" as a stacked series
     * @param showAbsent  whether to include "Absent" as a stacked series
     * @param showPending whether to include "Pending" as a stacked series
     */
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

        // Per-course arrays store counts in order: [Present, Late, Absent, Pending].
        Map<String, int[]> perCourse = new LinkedHashMap<>();
        for (AttendanceRecord r : records) {
            String course = (r.courseCode != null && !r.courseCode.isBlank())
                    ? r.courseCode : "Unknown";
            perCourse.putIfAbsent(course, new int[4]);
            int[] arr = perCourse.get(course);

            // Normalize status and increment the appropriate index.
            String st = (r.status == null || r.status.isEmpty()) ? "Pending" : r.status;
            if ("Present".equals(st)) arr[0]++;
            else if ("Late".equals(st)) arr[1]++;
            else if ("Absent".equals(st)) arr[2]++;
            else arr[3]++;
        }

        // No data → clear chart and exit.
        if (perCourse.isEmpty()) {
            chart.getData().clear();
            return;
        }

        // Preserve insertion order of courses to keep chart stable and predictable.
        List<String> courses = new ArrayList<>(perCourse.keySet());
        x.setCategories(FXCollections.observableArrayList(courses));

        // Dynamically build series based on which statuses should be displayed.
        List<XYChart.Series<String, Number>> seriesList = new ArrayList<>();

        if (showPresent) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName("Present");
            for (String c : courses) {
                s.getData().add(new XYChart.Data<>(c, perCourse.get(c)[0]));
            }
            seriesList.add(s);
        }
        if (showLate) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName("Late");
            for (String c : courses) {
                s.getData().add(new XYChart.Data<>(c, perCourse.get(c)[1]));
            }
            seriesList.add(s);
        }
        if (showAbsent) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName("Absent");
            for (String c : courses) {
                s.getData().add(new XYChart.Data<>(c, perCourse.get(c)[2]));
            }
            seriesList.add(s);
        }
        if (showPending) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName("Pending");
            for (String c : courses) {
                s.getData().add(new XYChart.Data<>(c, perCourse.get(c)[3]));
            }
            seriesList.add(s);
        }

        chart.getData().setAll(seriesList);
        STYLER.styleStackedBarChart(chart);
        addStackedTooltips(chart);

        // Build a custom legend based on the series that are actually shown.
        if (legendBox != null) {
            legendBox.getChildren().setAll(
                    LEGENDS.buildLegendNodesFromSeries(seriesList)
            );
        }
    }

    /* ================= TOOLTIP HELPERS ================= */

    /**
     * Attach tooltips to each bar in a bar chart.
     *
     * <p>Tooltip text format:
     * <pre>
     * SeriesName
     * XValue: YValue
     * </pre>
     * If a series has no name, {@code fallback} is used instead.
     *
     * @param chart    the bar chart whose data nodes should be decorated
     * @param fallback fallback label for unnamed series
     */
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

    /**
     * Attach tooltips to each slice in a pie chart, showing label, count and percentage.
     *
     * <p>Tooltip text format:
     * <pre>
     * Name
     * Count (XX.X%)
     * </pre>
     *
     * @param pie the pie chart whose slices should be decorated
     */
    private static void addPieTooltips(PieChart pie) {
        // Compute total for percentage calculation.
        double total = 0;
        for (PieChart.Data d : pie.getData()) total += d.getPieValue();

        // For each slice, compute its percentage of the total and install a tooltip.
        for (PieChart.Data d : pie.getData()) {
            Node n = d.getNode();
            if (n == null) continue;
            double pct = total > 0 ? (d.getPieValue() * 100.0 / total) : 0.0;
            String txt = String.format("%s%n%d (%.1f%%)", d.getName(), (int) d.getPieValue(), pct);
            Tooltip.install(n, new Tooltip(txt));
        }
    }

    /**
     * Attach tooltips to each segment in a stacked bar chart.
     *
     * <p>Tooltip text format:
     * <pre>
     * SeriesName
     * XValue: YValue
     * </pre>
     *
     * @param chart the stacked bar chart whose data nodes should be decorated
     */
    private static void addStackedTooltips(StackedBarChart<String, Number> chart) {
        for (XYChart.Series<String, Number> s : chart.getData()) {
            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() == null) continue;
                Tooltip.install(
                        d.getNode(),
                        new Tooltip(s.getName() + "\n" + d.getXValue() + ": " + d.getYValue())
                );
            }
        }
    }
}
