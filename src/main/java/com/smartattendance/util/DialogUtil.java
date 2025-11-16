package com.smartattendance.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utility class for displaying standardised dialog boxes in the JavaFX
 * application.
 * 
 * This class provides a simplified interface for common dialog types used
 * throughout
 * the Smart Attendance System, ensuring consistent user experience and reducing
 * code duplication for dialog creation.
 * 
 * Supported dialog types:
 * - Confirmation dialogs - for user decisions with OK/Cancel options
 * - Warning dialogs - for non-critical issues that require user acknowledgment
 * - Error dialogs - for critical errors that need immediate attention
 * 
 * All dialogs are modal and block user interaction until dismissed, ensuring
 * important messages are not overlooked.
 * 
 * @author Lim Jia Hui
 * @version 19:13 16 Nov 2025
 */
public class DialogUtil {

    // ========== CONFIRMATION DIALOG METHODS ==========

    /**
     * Creates and configures a confirmation dialog with custom title, header, and
     * content.
     * 
     * This dialog presents the user with an OK/Cancel choice and returns
     * their decision. The dialog is modal and must be dismissed before the
     * application can continue.
     *
     * @param title   the title text displayed in the dialog window title bar
     * @param header  the header text displayed prominently at the top of the
     *                dialog,
     *                or null for no header
     * @param content the detailed message content displayed in the main body of the
     *                dialog
     * @return true if the user clicked OK, false if they clicked Cancel or closed
     *         the dialog
     * @throws IllegalArgumentException if title or content is null or empty
     * 
     * @example
     * 
     *          <pre>
     * {@code
     * boolean confirmed = dialogUtil.showConfirmation(
     *     "Delete Session",
     *     "Confirm Deletion",
     *     "Are you sure you want to delete this session? This action cannot be undone."
     * );
     * if (confirmed) {
     *     // Proceed with deletion
     * }
     * }
     * </pre>
     */
    public boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // ========== WARNING DIALOG METHODS ==========

    /**
     * Create and configures a warning dialog with a custom message.
     * 
     * Warning dialogs are used for non-critical issues that the user should
     * acknowledge but don't require a decision. The dialog displays a warning
     * icon and must be dismissed by clicking OK.
     *
     * @param message the warning message to display in the dialog body
     * @throws IllegalArgumentException if message is null or empty
     * 
     * @example
     * 
     *          <pre>
     * {@code
     * dialogUtil.showWarning("Session will automatically close in 5 minutes.");
     * }
     * </pre>
     */
    public void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Create and configures an error dialog with a custom message.
     * 
     * Error dialogs are used for critical errors that prevent normal operation
     * or indicate serious problems. The dialog displays an error icon and must
     * be dismissed by clicking OK.
     *
     * @param message the error message to display in the dialog body
     * @throws IllegalArgumentException if message is null or empty
     * 
     * @example
     * 
     *          <pre>
     * {@code
     * try {
     *     // Some operation that might fail
     * } catch (Exception e) {
     *     dialogUtil.showError("Failed to save session: " + e.getMessage());
     * }
     * }
     * </pre>
     */
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}