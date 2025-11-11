package com.smartattendance.util.chart;

import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;

public class DefaultChartStyler extends AbstractChartStyler {

    @Override
    public void styleBarChart(BarChart<?, ?> chart) {
        if (chart == null) return;
        chart.setLegendVisible(false);
        styleNumberAxis(chart.getYAxis());

        // apply colors
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

    @Override
    public void styleStackedBarChart(StackedBarChart<?, ?> chart) {
        if (chart == null) return;
        chart.setLegendVisible(false);
        styleNumberAxis(chart.getYAxis());

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

    @Override
    public void stylePieChart(PieChart pieChart) {
        if (pieChart == null) return;
        pieChart.setLegendVisible(false);
        int i = 0;
        for (PieChart.Data d : pieChart.getData()) {
            Node n = d.getNode();
            if (n != null) {
                n.setStyle("-fx-pie-color: " + colorAt(i) + ";");
            }
            i++;
        }
    }
}
