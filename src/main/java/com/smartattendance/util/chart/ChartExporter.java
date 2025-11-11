package com.smartattendance.util.chart;

import javafx.scene.Node;

public interface ChartExporter {
    void export(Node chartNode, String baseName, boolean includeLegend);
}
