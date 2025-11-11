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

public class DefaultLegendBuilder implements LegendBuilder {

    private static final String[] COLORS = {
            "#10b981", "#f59e0b", "#ef4444", "#94a3b8", "#3b82f6"
    };

    @Override
    public List<Node> buildLegendNodes(List<PieChart.Data> data) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            PieChart.Data d = data.get(i);
            nodes.add(makeLegendRow(d.getName(), COLORS[i % COLORS.length]));
        }
        return nodes;
    }

    @Override
    public List<Node> buildLegendNodesFromSeries(List<XYChart.Series<String, Number>> seriesList) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < seriesList.size(); i++) {
            XYChart.Series<String, Number> s = seriesList.get(i);
            String name = (s.getName() != null) ? s.getName() : ("Series " + (i+1));
            nodes.add(makeLegendRow(name, COLORS[i % COLORS.length]));
        }
        return nodes;
    }

    private Node makeLegendRow(String name, String color) {
        Region swatch = new Region();
        swatch.setMinSize(12,12);
        swatch.setPrefSize(12,12);
        swatch.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");
        Label label = new Label(name);
        label.setStyle("-fx-font-weight: 600;");
        HBox box = new HBox(6, swatch, label);
        box.setPadding(new Insets(2,6,2,2));
        return box;
    }
}
