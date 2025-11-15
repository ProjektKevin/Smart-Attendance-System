package com.smartattendance.util.chart;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;

/**
 * ChartStyler defines a small abstraction layer for applying consistent
 * visual styling to JavaFX charts.
 *
 * <p>By depending on this interface instead of directly manipulating the
 * chart properties in controllers, the dashboard can:
 * <ul>
 *     <li>Remain decoupled from styling details</li>
 *     <li>Centralize theme-related logic in one place</li>
 *     <li>Easily support different themes (e.g., light / dark mode)</li>
 * </ul>
 *
 * <p>Typical usage:
 * <pre>
 *     // In a controller or service:
 *     chartStyler.styleBarChart(attendanceBarChart);
 * </pre>
 */
public interface ChartStyler {

    /**
     * Apply a consistent visual style to a standard {@link BarChart}.
     * <p>
     * Implementations might set:
     * <ul>
     *     <li>Axis label fonts and tick label visibility</li>
     *     <li>Bar spacing, category gap, and legend visibility</li>
     *     <li>Series colors or CSS style classes</li>
     * </ul>
     *
     * @param chart the bar chart instance to be styled; must not be {@code null}
     */
    void styleBarChart(BarChart<?, ?> chart);

    /**
     * Apply a consistent visual style to a {@link StackedBarChart}.
     * <p>
     * Implementations might:
     * <ul>
     *     <li>Adjust bar width and category spacing</li>
     *     <li>Configure legend and axis visibility for readability</li>
     *     <li>Assign a color scheme that differentiates stacked segments clearly</li>
     * </ul>
     *
     * @param chart the stacked bar chart instance to be styled; must not be {@code null}
     */
    void styleStackedBarChart(StackedBarChart<?, ?> chart);

    /**
     * Apply a consistent visual style to a {@link PieChart}.
     * <p>
     * Implementations might:
     * <ul>
     *     <li>Configure whether labels are shown on slices or in a legend</li>
     *     <li>Set start angle and animation behaviour</li>
     *     <li>Apply CSS classes or colors that match the application theme</li>
     * </ul>
     *
     * @param pieChart the pie chart instance to be styled; must not be {@code null}
     */
    void stylePieChart(PieChart pieChart);
}
