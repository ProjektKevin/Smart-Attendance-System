package com.smartattendance.util.chart;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * {@code PngChartExporter} is a simple implementation of {@link ChartExporter}
 * that exports a JavaFX {@link Node} (typically a chart container) as a PNG file.
 *
 * <p>Key characteristics:
 * <ul>
 *     <li>Exports at a fixed 2x scale to reduce blurriness in the output image</li>
 *     <li>Uses a {@link FileChooser} dialog so users can choose the save location</li>
 *     <li>Relies on JavaFX snapshot + AWT/Swing {@link ImageIO} for encoding</li>
 * </ul>
 *
 * <p>This class implements the project's {@code ChartExporter} interface:
 * <pre>
 *     void export(Node node, String suggestedName, boolean ignoreLegend);
 * </pre>
 * The {@code ignoreLegend} flag is not used here; the entire node is exported as-is.
 */
public class PngChartExporter implements ChartExporter {

    /**
     * Scale factor applied to the node when taking the snapshot.
     *
     * <p>Using a scale &gt; 1.0 increases the resolution of the snapshot,
     * which helps avoid a blurry look when the PNG is viewed or embedded
     * in documents (e.g. reports, slides).
     */
    private static final double SCALE = 2.0;

    /**
     * Export the given node as a PNG file.
     *
     * <p>This method satisfies the {@link ChartExporter} contract and simply
     * delegates to {@link #exportNode(Node, String)}. The {@code ignoreLegend}
     * parameter is currently ignored; callers are expected to pass a node that
     * already reflects whatever legend behaviour they want (included or removed).
     *
     * @param node          the JavaFX node to export (typically a chart container)
     * @param suggestedName the suggested base file name (without extension)
     * @param ignoreLegend  flag from the interface; ignored in this implementation
     */
    @Override
    public void export(Node node, String suggestedName, boolean ignoreLegend) {
        exportNode(node, suggestedName);
    }

    /**
     * Take a high-resolution snapshot of the given {@link Node} and prompt
     * the user to save it as a PNG file.
     *
     * <p>Steps:
     * <ol>
     *     <li>Return immediately if {@code node} is {@code null}</li>
     *     <li>Create {@link SnapshotParameters} with a 2x scale transform</li>
     *     <li>Snapshot the node into a {@link WritableImage}</li>
     *     <li>Open a {@link FileChooser} configured for PNG export</li>
     *     <li>Write the image to disk using {@link ImageIO}</li>
     * </ol>
     *
     * @param node          the JavaFX node to snapshot; if {@code null}, nothing happens
     * @param suggestedName the suggested base file name (without extension);
     *                      may be {@code null} or empty, in which case the dialog
     *                      will still show but without a nice default name
     */
    public static void exportNode(Node node, String suggestedName) {
        // Defensive check: if there is no node, there is nothing to export.
        if (node == null) return;

        // Configure snapshot parameters with a scale transform so that the
        // resulting PNG has a higher resolution than the on-screen rendering.
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(javafx.scene.transform.Transform.scale(SCALE, SCALE));

        // Capture the node's current visual state into an off-screen image.
        WritableImage image = node.snapshot(params, null);

        // Prepare a file chooser dialog to let the user choose where to save.
        FileChooser fc = new FileChooser();
        fc.setTitle("Export chart");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );

        // If a suggested name is provided, use it as the default file name.
        // The ".png" extension is appended here.
        fc.setInitialFileName(suggestedName + ".png");

        // Try to use the node's window as the owner for the dialog, if available.
        Window w = (node.getScene() != null) ? node.getScene().getWindow() : null;

        // Show the save dialog; if the user cancels, no file will be returned.
        File file = fc.showSaveDialog(w);
        if (file == null) return;

        // Encode the image as PNG and write it to the chosen file.
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            // In this project, we simply print the stack trace. In a production
            // app, you might show a user-facing error dialog or log it properly.
            e.printStackTrace();
        }
    }
}
