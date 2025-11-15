package com.smartattendance.util;

import javafx.scene.control.Label;

public class InfoLabelUtil {
    // Styles the info label based on message type
    // @param type "success", "error", "warning", or "normal"
    // @param message The text to display
    private static void styleInfoLabel(Label label, String type, String message) {
        label.setText(message);

        // Reset styles first
        label.setStyle("-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        switch (type.toLowerCase()) {
            case "success":
                label
                        .setStyle("-fx-text-fill: #155724; -fx-background-color: #d4edda; -fx-border-color: #c3e6cb; " +
                                "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
            case "error":
                label
                        .setStyle("-fx-text-fill: #721c24; -fx-background-color: #f8d7da; -fx-border-color: #f5c6cb; " +
                                "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
            case "warning":
                label
                        .setStyle("-fx-text-fill: #856404; -fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; " +
                                "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
            case "normal":
            default:
                label
                        .setStyle("-fx-text-fill: #383d41; -fx-background-color: #e2e3e5; -fx-border-color: #d6d8db; " +
                                "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
        }
    }

    // Helper methods for different message types
    public static void showSuccess(Label label, String message) {
        styleInfoLabel(label, "success", "✓ " + message);
    }

    public static void showError(Label label, String message) {
        styleInfoLabel(label, "error", "✗ " + message);
    }

    public static void showWarning(Label label, String message) {
        styleInfoLabel(label, "warning", "⚠ " + message);
    }

    public static void showInfo(Label label, String message) {
        styleInfoLabel(label, "normal", "ℹ " + message);
    }
}
