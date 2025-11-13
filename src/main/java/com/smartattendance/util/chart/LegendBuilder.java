package com.smartattendance.util.chart;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

/**
 * LegendBuilder defines an abstraction for generating custom legend nodes
 * for charts.
 *
 * <p>Instead of relying on the built-in JavaFX chart legend, implementations
 * of this interface can:
 * <ul>
 *     <li>Render legends using arbitrary JavaFX nodes (e.g. HBox, VBox)</li>
 *     <li>Control layout, colors, and typography consistently across the app</li>
 *     <li>Reuse the same legend logic for different chart types</li>
 * </ul>
 *
 * <p>Typical usage:
 * <pre>
 *     LegendBuilder legendBuilder = new DefaultLegendBuilder();
 *     List&lt;Node&gt; nodes = legendBuilder.buildLegendNodes(pieChart.getData());
 *     legendContainer.getChildren().setAll(nodes);
 * </pre>
 */
public interface LegendBuilder {

    /**
     * Build a list of legend nodes corresponding to the given
     * {@link PieChart.Data} items.
     *
     * <p>Each element in the returned list represents a single legend entry
     * (for example, a row with a colored swatch and a label). The order of
     * entries should match the order of the data items.
     *
     * @param data list of pie chart data items to represent in the legend;
     *             expected to be non-{@code null}
     * @return a list of JavaFX {@link Node} instances, one per legend entry
     */
    List<Node> buildLegendNodes(List<PieChart.Data> data);

    /**
     * Build a list of legend nodes from a list of {@link XYChart.Series}
     * instances, typically used for bar or stacked bar charts.
     *
     * <p>Each series in {@code seriesList} becomes one legend entry. The
     * implementation may use the series name, a fallback name, and a
     * consistent color mapping when constructing the legend nodes.
     *
     * @param seriesList list of series to represent in the legend;
     *                   expected to be non-{@code null}
     * @return a list of JavaFX {@link Node} instances, one per series
     */
    List<Node> buildLegendNodesFromSeries(List<XYChart.Series<String, Number>> seriesList);
}
