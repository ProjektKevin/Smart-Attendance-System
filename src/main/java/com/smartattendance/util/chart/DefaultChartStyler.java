package com.smartattendance.util.chart;

import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;

/**
 * DefaultChartStyler is a concrete implementation of {@link AbstractChartStyler}
 * that applies a simple, consistent visual style to the application's charts.
 *
 * <p>Responsibilities:
 * <ul>
 *     <li>Hide legends when the surrounding UI already provides context</li>
 *     <li>Delegate numeric axis formatting to {@link AbstractChartStyler#styleNumberAxis}</li>
 *     <li>Assign colors to series / slices using {@link AbstractChartStyler#colorAt(int)}</li>
 * </ul>
 *
 * <p>This class is intended to be the "default" theme used by the dashboard,
 * but it can be swapped for another implementation (e.g. a dark theme) without
 * changing controller code.
 * 
 * @author Ernest Lun
 */
public class DefaultChartStyler extends AbstractChartStyler {

    /**
     * Apply default styling to a standard {@link BarChart}.
     *
     * <p>Behaviour:
     * <ul>
     *     <li>Ignores {@code null} charts (defensive no-op)</li>
     *     <li>Hides the legend so the dashboard can provide its own labels</li>
     *     <li>Formats the numeric Y-axis using {@link #styleNumberAxis}</li>
     *     <li>Assigns a distinct color to each data series via {@link #colorAt(int)}</li>
     * </ul>
     *
     * @param chart the bar chart to style; if {@code null}, the method returns immediately
     */
    @Override
    public void styleBarChart(BarChart<?, ?> chart) {
        // Defensive check so callers can safely pass charts that may not
        // have been initialized yet (e.g. FXML injection timing issues).
        if (chart == null) return;

        // The legend is often redundant in the dashboard layout, so hide it
        // for a cleaner UI. Series meaning can be explained elsewhere.
        chart.setLegendVisible(false);

        // Ensure the numeric Y-axis has consistent formatting (tick labels,
        // grid lines, font, etc.). The concrete logic lives in the base class.
        styleNumberAxis(chart.getYAxis());

        // Apply a color palette to the bars, one color per series.
        int si = 0;
        for (Object o : chart.getData()) {
            // Chart data is exposed with wildcards, so we cast with suppression.
            @SuppressWarnings("unchecked")
            XYChart.Series<?, Number> s = (XYChart.Series<?, Number>) o;

            // Choose a color for the current series index.
            String color = colorAt(si++);

            // Each data point corresponds to a bar node in the scene graph.
            for (XYChart.Data<?, Number> d : s.getData()) {
                Node n = d.getNode();
                if (n != null) {
                    // Inline style sets the bar fill color. This is simple and
                    // works well with a small, known set of charts.
                    n.setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        }
    }

    /**
     * Apply default styling to a {@link StackedBarChart}.
     *
     * <p>Behaviour:
     * <ul>
     *     <li>Ignores {@code null} charts (defensive no-op)</li>
     *     <li>Hides the legend to match the dashboard design</li>
     *     <li>Formats the numeric Y-axis consistently via {@link #styleNumberAxis}</li>
     *     <li>Assigns a distinct color to each series so stacked segments are distinguishable</li>
     * </ul>
     *
     * @param chart the stacked bar chart to style; if {@code null}, the method returns immediately
     */
    @Override
    public void styleStackedBarChart(StackedBarChart<?, ?> chart) {
        if (chart == null) return;

        chart.setLegendVisible(false);
        styleNumberAxis(chart.getYAxis());

        // Same color assignment logic as regular bar charts: each series
        // gets its own color, which then appears as stacked segments.
        int si = 0;
        for (Object o : chart.getData()) {
            @SuppressWarnings("unchecked")
            XYChart.Series<?, Number> s = (XYChart.Series<?, Number>) o;
            String color = colorAt(si++);
            for (XYChart.Data<?, Number> d : s.getData()) {
                Node n = d.getNode();
                if (n != null) {
                    n.setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        }
    }

    /**
     * Apply default styling to a {@link PieChart}.
     *
     * <p>Behaviour:
     * <ul>
     *     <li>Ignores {@code null} charts (defensive no-op)</li>
     *     <li>Hides the legend to keep the chart compact on the dashboard</li>
     *     <li>Assigns slice colors using the shared {@link #colorAt(int)} palette</li>
     * </ul>
     *
     * @param pieChart the pie chart to style; if {@code null}, the method returns immediately
     */
    @Override
    public void stylePieChart(PieChart pieChart) {
        if (pieChart == null) return;

        pieChart.setLegendVisible(false);

        // Assign a unique color to each slice based on its index.
        int i = 0;
        for (PieChart.Data d : pieChart.getData()) {
            Node n = d.getNode();
            if (n != null) {
                // -fx-pie-color controls the fill color of the slice.
                n.setStyle("-fx-pie-color: " + colorAt(i) + ";");
            }
            i++;
        }
    }
}
