package com.smartattendance.util.chart;

import javafx.scene.Node;

/**
 * Strategy interface for exporting charts to external formats.
 *
 * Implementations of this interface know how to take a rendered JavaFX chart
 * (or any {@link Node} that represents a chart) and export it to one or more
 * output formats, such as PNG images or PDF files.
 *
 * By using this interface, the rest of the application can trigger chart
 * exports without depending on any specific export library or file format.
 * 
 * @author Ernest Lun
 */
public interface ChartExporter {

    /**
     * Exports the given chart node to a file (or set of files).
     *
     * Implementations are free to decide:
     * <ul>
     *     <li>Which file format(s) to generate (e.g. PNG, JPG, PDF).</li>
     *     <li>How to derive the full filename(s) from {@code baseName}.</li>
     *     <li>How to handle the {@code includeLegend} flag (for example,
     *         by including or excluding the legend area from the exported
     *         image).</li>
     * </ul>
     *
     * @param chartNode     the JavaFX node representing the chart to export.
     * @param baseName      the base name to use for the exported file(s),
     *                      typically without an extension.
     * @param includeLegend whether the exported output should include the
     *                      chart legend, if present.
     */
    void export(Node chartNode, String baseName, boolean includeLegend);
}
