package com.smartattendance.util.chart;

import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.paint.Color;

/**
 * Base class for chart styling helpers.
 *
 * This abstract class provides common styling utilities that can be reused
 * by concrete {@link ChartStyler} implementations for different charts
 * (e.g., line charts, bar charts, etc.).
 *
 * It defines a shared color palette and helper methods for:
 * <ul>
 *     <li>Styling numeric axes in a consistent way.</li>
 *     <li>Choosing colors from a palette by index.</li>
 *     <li>Converting hex colors to CSS-compatible rgba() strings.</li>
 * </ul>
 *
 * By centralizing these utilities, we keep chart styling consistent across
 * the application and avoid duplicating color and axis configuration logic
 * in each individual chart class.
 */
public abstract class AbstractChartStyler implements ChartStyler {

    /**
     * Shared color palette used for chart series and accents.
     *
     * Colors are defined as hex strings and can be reused across different
     * charts to maintain a cohesive visual style throughout the application.
     */
    protected static final String[] PALETTE = {
            "#10b981", // green
            "#f59e0b", // amber
            "#ef4444", // red
            "#94a3b8", // slate
            "#3b82f6"  // blue
    };

    /**
     * Applies a consistent style to numeric axes.
     *
     * If the given axis is an instance of {@link NumberAxis}, this method:
     * <ul>
     *     <li>Disables minor tick marks.</li>
     *     <li>Hides tick mark lines.</li>
     *     <li>Hides minor tick marks.</li>
     * </ul>
     *
     * This results in a cleaner, less cluttered appearance, which works well
     * for dashboards and summary charts where precise minor ticks are not
     * necessary.
     *
     * @param axis the axis to style; if it is not a {@link NumberAxis},
     *             this method does nothing.
     */
    protected void styleNumberAxis(Axis<?> axis) {
        if (axis instanceof NumberAxis) {
            NumberAxis na = (NumberAxis) axis;
            na.setMinorTickCount(0);
            na.setTickMarkVisible(false);
            na.setMinorTickVisible(false);
        }
    }

    /**
     * Selects a color from the shared palette based on the given index.
     *
     * The index is wrapped using modulo arithmetic so that callers can
     * request colors for any non-negative index without worrying about
     * the palette length.
     *
     * @param i the requested color index (0-based).
     * @return a hex color string from the palette, e.g. "#10b981".
     */
    protected String colorAt(int i) {
        return PALETTE[i % PALETTE.length];
    }

    /**
     * Converts a hex color code and alpha value into a CSS rgba() string.
     *
     * This is useful when styling chart nodes using inline CSS, especially
     * for semi-transparent fills (e.g. area under a line, hover highlights).
     *
     * @param hex   a hex color string, e.g. "#10b981".
     * @param alpha the opacity component between 0.0 (fully transparent)
     *              and 1.0 (fully opaque).
     * @return a CSS rgba() string in the form "rgba(r,g,b,a)".
     */
    protected String rgba(String hex, double alpha) {
        Color c = Color.web(hex);
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255),
                alpha);
    }
}
