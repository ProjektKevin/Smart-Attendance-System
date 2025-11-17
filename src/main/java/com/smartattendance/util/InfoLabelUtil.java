package com.smartattendance.util;

import javafx.scene.control.Label;

/**
 * Utility class for styling and displaying status messages in JavaFX Label
 * components.
 * 
 * This utility provides standardized styling for different types of status
 * messages
 * (success, error, warning, info) with consistent colors, icons, and formatting
 * throughout the application.
 * 
 * Features:
 * - Consistent color schemes following UX best practices
 * - Visual icons for quick message type recognition
 * - Standardised padding and border styling
 * - Accessible color contrast ratios
 * - Easy-to-use static methods
 * 
 * Color Scheme Reference:
 * - Success: Green theme (#155724 text, #d4edda background)
 * - Error: Red theme (#721c24 text, #f8d7da background)
 * - Warning: Yellow theme (#856404 text, #fff3cd background)
 * - Info: Gray theme (#383d41 text, #e2e3e5 background)
 * 
 * @author Lim Jia Hui
 * @version 20:35 16 Nov 2025
 */
public class InfoLabelUtil {

    // ========== PRIVATE STYLING METHOD ==========

    /**
     * Applies the appropriate CSS style based on the message type.
     *
     * @param label the label to style
     * @param type  the message type
     * @throws IllegalArgumentException if the message type is not recognized
     */
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

    // ========== PUBLIC HELPER METHODS ==========

    /**
     * Displays a success message with green styling and check mark icon.
     * 
     * Use for positive confirmations, successful operations, or completed tasks.
     *
     * @param label   the Label component to display the message in
     * @param message the success message to display (without icon)
     * @throws IllegalArgumentException if label or message is null
     * 
     * @example
     * 
     *          <pre>
     * {@code
     * InfoLabelUtil.showSuccess(statusLabel, "Session created successfully");
     * // Displays: "✓ Session created successfully" in green
     * }
     * </pre>
     */
    public static void showSuccess(Label label, String message) {
        styleInfoLabel(label, "success", "✓ " + message);
    }

    /**
     * Displays an error message with red styling and cross mark icon.
     * 
     * Use for critical errors, failed operations, or problems requiring immediate
     * attention.
     *
     * @param label   the Label component to display the message in
     * @param message the error message to display (without icon)
     * @throws IllegalArgumentException if label or message is null
     * 
     * @example
     * 
     *          <pre>
     * {@code
     * InfoLabelUtil.showError(statusLabel, "Failed to save session");
     * // Displays: "✗ Failed to save session" in red
     * }
     * </pre>
     */
    public static void showError(Label label, String message) {
        styleInfoLabel(label, "error", "✗ " + message);
    }

    /**
     * Displays a warning message with yellow styling and warning icon.
     * 
     * Use for non-critical warnings, potential issues, or important notices.
     *
     * @param label   the Label component to display the message in
     * @param message the warning message to display (without icon)
     * @throws IllegalArgumentException if label or message is null
     * 
     * @example
     * 
     *          <pre>
     * {@code
     * InfoLabelUtil.showWarning(statusLabel, "Session will auto-close in 5 minutes");
     * // Displays: "⚠ Session will auto-close in 5 minutes" in yellow
     * }
     * </pre>
     */
    public static void showWarning(Label label, String message) {
        styleInfoLabel(label, "warning", "⚠ " + message);
    }

    /**
     * Displays an informational message with gray styling and info icon.
     * 
     * Use for general information, status updates, or neutral notifications.
     *
     * @param label   the Label component to display the message in
     * @param message the informational message to display (without icon)
     * @throws IllegalArgumentException if label or message is null
     * 
     * @example
     * 
     *          <pre>
     * {@code
     * InfoLabelUtil.showInfo(statusLabel, "3 students marked present");
     * // Displays: "ℹ 3 students marked present" in gray
     * }
     * </pre>
     */
    public static void showInfo(Label label, String message) {
        styleInfoLabel(label, "normal", "ℹ " + message);
    }
}
