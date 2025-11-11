package com.smartattendance.util.chart;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;

/** Simple interface so dashboard can stay decoupled. */
public interface ChartStyler {
    void styleBarChart(BarChart<?,?> chart);
    void styleStackedBarChart(StackedBarChart<?,?> chart);
    void stylePieChart(PieChart pieChart);
}
