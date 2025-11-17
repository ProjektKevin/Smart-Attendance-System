package com.smartattendance.controller.student;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Utility class to build the legend bar shown on the student attendance screen.
 *
 * <p>
 * The legend visually explains the row highlight colors used in the
 * {@code TableView}, mapping each attendance status (Present, Late, Absent,
 * Pending) to a small colored "chip" and label.
 * </p>
 *
 * <p>
 * This class is stateless and provides a single static {@link #build()} method
 * that can be used directly from controllers or FXML initialization code.
 * </p>
 *
 * @author Ernest Lun
 */
public class StudentAttendanceLegendBuilder {

    /**
     * Builds the root {@link HBox} containing the attendance legend.
     *
     * <p>
     * The returned layout has the following structure:
     * <ul>
     *     <li>A "Legend:" label</li>
     *     <li>A series of colored chips with labels for each status:
     *         Present, Late, Absent, Pending</li>
     * </ul>
     * The colors are chosen to match the row background colors configured in
     * {@code StudentAttendanceController}.
     * </p>
     *
     * @return an {@link HBox} ready to be added to the scene graph
     */
    public static HBox build() {
        HBox root = new HBox(12);
        // Top padding so the legend aligns nicely with surrounding content.
        root.setStyle("-fx-padding: 2 0 0 0;");

        root.getChildren().add(new Label("Legend:"));
        root.getChildren().add(item("Present",
                "rgba(46,125,50,0.10)", "rgba(46,125,50,0.45)"));
        root.getChildren().add(item("Late",
                "rgba(245,124,0,0.10)", "rgba(245,124,0,0.45)"));
        root.getChildren().add(item("Absent",
                "rgba(198,40,40,0.10)", "rgba(198,40,40,0.45)"));
        root.getChildren().add(item("Pending",
                "rgba(97,97,97,0.06)", "rgba(97,97,97,0.35)"));
        return root;
    }

    /**
     * Creates a single legend item composed of a colored chip and a text label.
     *
     * @param text   label to display next to the chip (e.g. "Present")
     * @param bg     CSS color for the chip background (e.g. {@code rgba(...)})
     * @param border CSS color for the chip border
     * @return an {@link HBox} containing the chip and its label
     */
    private static HBox item(String text, String bg, String border) {
        Region chip = new Region();
        chip.setPrefWidth(18);
        chip.setPrefHeight(12);
        chip.setStyle(
                "-fx-background-color: " + bg + ";" +
                "-fx-border-color: " + border + ";" +
                "-fx-border-radius: 3;" +
                "-fx-background-radius: 3;"
        );

        // Horizontal spacing between the chip and its label.
        return new HBox(5, chip, new Label(text));
    }
}
