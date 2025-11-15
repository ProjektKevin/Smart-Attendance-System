package com.smartattendance.util;

import java.io.IOException;

import com.smartattendance.controller.AttendanceController;
import com.smartattendance.model.entity.Session;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SessionViewNavigator {

    private final VBox mainContainer; // sessionListContainer
    private final VBox placeholderContainer; // attendanceViewContainer

    private AttendanceController attendanceController;

    public SessionViewNavigator(VBox mainContainer, VBox placeholderContainer) {
        this.mainContainer = mainContainer;
        this.placeholderContainer = placeholderContainer;
    }

    /**
     * Opens the attendance view for a session.
     * 
     * @param fxmlPath FXML file path for AttendanceView
     * @param session  The session to open
     * @throws IOException If FXML fails to load
     */
    public void openAttendanceView(String fxmlPath, Session session) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Get the attendance controller
        attendanceController = loader.getController();
        attendanceController.setSession(session);

        // Provide back handler
        attendanceController.setBackHandler(() -> {
            placeholderContainer.getChildren().clear();
            placeholderContainer.setVisible(false);
            placeholderContainer.setManaged(false);

            mainContainer.setVisible(true);
            mainContainer.setManaged(true);
        });

        // Show attendance view
        mainContainer.setVisible(false);
        mainContainer.setManaged(false);

        placeholderContainer.getChildren().setAll(root);
        placeholderContainer.setVisible(true);
        placeholderContainer.setManaged(true);
    }

    public AttendanceController getAttendanceController() {
        return attendanceController;
    }

    /**
     * Open a modal dialog view (e.g., session form)
     * 
     * @param fxmlPath Path to FXML
     * @param title    Dialog title
     * @return FXMLLoader so caller can access controller
     * @throws IOException
     */
    public FXMLLoader openModalDialog(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.setScene(new Scene(root));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();

        return loader;
    }
}
