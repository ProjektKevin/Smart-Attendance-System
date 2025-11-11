package com.smartattendance.controller.student;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class StudentAttendanceLegendBuilder {

    public static HBox build() {
        HBox root = new HBox(12);
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
        return new HBox(5, chip, new Label(text));
    }
}
