package com.smartattendance.util.chart;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * DefaultLegendBuilder is a concrete implementation of {@link LegendBuilder}
 * that constructs simple, custom legend nodes for charts.
 *
 * <p>Instead of relying on the built-in JavaFX chart legends, this builder
 * creates lightweight {@link HBox} rows consisting of a colored swatch and a
 * label. This makes it easier to:
 * <ul>
 *     <li>Place legends anywhere in the dashboard layout</li>
 *     <li>Apply consistent styling across different chart types</li>
 *     <li>Reuse the same color palette for pie charts and bar charts</li>
 * </ul>
 */
public class DefaultLegendBuilder implements LegendBuilder {

    /**
     * Shared color palette used for legend swatches.
     *
     * <p>The palette is intentionally small; when there are more data items
     * than colors, the colors are reused in a cyclic fashion using modulo.
     */
    private static final String[] COLORS = {
            "#10b981", "#f59e0b", "#ef4444", "#94a3b8", "#3b82f6"
    };

    /**
     * Build legend nodes for a list of {@link PieChart.Data} entries.
     *
     * <p>Each data item becomes one legend row displaying:
     * <ul>
     *     <li>A colored circular swatch (based on its index)</li>
     *     <li>The data name as a label</li>
     * </ul>
     *
     * @param data the list of pie chart data entries to represent in the legend
     * @return a list of JavaFX nodes, each representing one legend row
     */
    @Override
    public List<Node> buildLegendNodes(List<PieChart.Data> data) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            PieChart.Data d = data.get(i);
            // Use cyclic indexing into COLORS so the palette can be reused
            // even if there are more slices than colors.
            nodes.add(makeLegendRow(d.getName(), COLORS[i % COLORS.length]));
        }
        return nodes;
    }

    /**
     * Build legend nodes from a list of {@link XYChart.Series} instances,
     * typically used for bar charts or stacked bar charts.
     *
     * <p>Each series becomes one legend row displaying:
     * <ul>
     *     <li>A colored circular swatch (based on its index)</li>
     *     <li>The series name, or a fallback like "Series 1" if no name is set</li>
     * </ul>
     *
     * @param seriesList the list of series to represent in the legend
     * @return a list of JavaFX nodes, one per series
     */
    @Override
    public List<Node> buildLegendNodesFromSeries(List<XYChart.Series<String, Number>> seriesList) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < seriesList.size(); i++) {
            XYChart.Series<String, Number> s = seriesList.get(i);
            // Fallback to a generated name if the series name is missing.
            String name = (s.getName() != null) ? s.getName() : ("Series " + (i + 1));
            nodes.add(makeLegendRow(name, COLORS[i % COLORS.length]));
        }
        return nodes;
    }

    /**
     * Create a single legend row node with a color swatch and a label.
     *
     * <p>Layout:
     * <ul>
     *     <li>A small rounded {@link Region} as the color swatch</li>
     *     <li>A bold {@link Label} displaying the legend text</li>
     *     <li>Both placed horizontally inside an {@link HBox}</li>
     * </ul>
     *
     * @param name  the text to show next to the color swatch
     * @param color the CSS color value (e.g. {@code "#10b981"}) for the swatch
     * @return a fully configured {@link Node} representing a single legend entry
     */
    private Node makeLegendRow(String name, String color) {
        // Swatch: small rounded square indicating the series/slice color.
        Region swatch = new Region();
        swatch.setMinSize(12, 12);
        swatch.setPrefSize(12, 12);
        swatch.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");

        // Label: holds the legend text. Slightly bold to give visual weight.
        Label label = new Label(name);
        label.setStyle("-fx-font-weight: 600;");

        // HBox to align swatch and label horizontally with a small gap.
        HBox box = new HBox(6, swatch, label);
        box.setPadding(new Insets(2, 6, 2, 2));

        return box;
    }
}
