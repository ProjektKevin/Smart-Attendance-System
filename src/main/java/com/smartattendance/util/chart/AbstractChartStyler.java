package com.smartattendance.util.chart;

import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.paint.Color;

/**
 * Common bits for concrete stylers.
 */
public abstract class AbstractChartStyler implements ChartStyler {

    protected static final String[] PALETTE = {
            "#10b981", // green
            "#f59e0b", // amber
            "#ef4444", // red
            "#94a3b8", // slate
            "#3b82f6"  // blue
    };

    protected void styleNumberAxis(Axis<?> axis) {
        if (axis instanceof NumberAxis) {
            NumberAxis na = (NumberAxis) axis;
            na.setMinorTickCount(0);
            na.setTickMarkVisible(false);
            na.setMinorTickVisible(false);
        }
    }

    protected String colorAt(int i) {
        return PALETTE[i % PALETTE.length];
    }

    protected String rgba(String hex, double alpha) {
        Color c = Color.web(hex);
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int)(c.getRed()*255),
                (int)(c.getGreen()*255),
                (int)(c.getBlue()*255),
                alpha);
    }
}
