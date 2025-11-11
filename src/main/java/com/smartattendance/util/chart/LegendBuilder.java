package com.smartattendance.util.chart;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

public interface LegendBuilder {

    List<Node> buildLegendNodes(List<PieChart.Data> data);

    List<Node> buildLegendNodesFromSeries(List<XYChart.Series<String, Number>> seriesList);
}
