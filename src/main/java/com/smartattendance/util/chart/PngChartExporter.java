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
 * PNG exporter with 2x scale so it isn't blurry.
 * Implements project's ChartExporter (export(Node, String, boolean)).
 */
public class PngChartExporter implements ChartExporter {

    private static final double SCALE = 2.0;

    @Override
    public void export(Node node, String suggestedName, boolean ignoreLegend) {
        exportNode(node, suggestedName);
    }

    public static void exportNode(Node node, String suggestedName) {
        if (node == null) return;

        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(javafx.scene.transform.Transform.scale(SCALE, SCALE));

        WritableImage image = node.snapshot(params, null);

        FileChooser fc = new FileChooser();
        fc.setTitle("Export chart");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        fc.setInitialFileName(suggestedName + ".png");

        Window w = node.getScene() != null ? node.getScene().getWindow() : null;
        File file = fc.showSaveDialog(w);
        if (file == null) return;

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
