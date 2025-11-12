package com.smartattendance.controller;

import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.enums.ToastType;
import com.smartattendance.service.AttendanceObserver;
import com.smartattendance.service.RecognitionService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class LiveRecognitionController implements AttendanceObserver {

    @FXML
    private ImageView cameraView;
    @FXML
    private Label statusLabel;
    // F_MA: added by felicia handling marking attendance
    // @FXML
    // private Label alertLabel;
    @FXML
    private Pane toastPane;
    // @FXML
    // private Label alertLabel;
    private final RecognitionService recognitionService = new RecognitionService();
    

    @FXML
    private void initialize() {
        recognitionService.getAttendanceService().addObserver(this);
        statusLabel.setText("Idle");
    }

    @FXML
    private void onStartCamera() {
        statusLabel.setText("Starting (stub)...");
        recognitionService.startRecognition();
    }

    @FXML
    private void onStopCamera() {
        statusLabel.setText("Stopped (stub).");
        recognitionService.stopRecognition();
    }

    // F_MA: added by felicia handling marking attendance
    // public enum ToastType {
    //     SUCCESS, ERROR, WARNING
    // }

    public void showToast(String message, ToastType type) {
        Platform.runLater(() -> {
            // Create a label for the toast
            Label toast = new Label(message);
            toast.setStyle("-fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 5; -fx-font-weight: bold;");

            // Set background color based on type
            switch (type) {
                case SUCCESS ->
                    // toast.setStyle(toast.getStyle() + "-fx-background-color: #4BB543;"); // Green
                    toast.setStyle("-fx-text-fill: #155724; -fx-background-color: #d4edda; -fx-border-color: #c3e6cb; "
                            + "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                case ERROR ->
                    // toast.setStyle(toast.getStyle() + "-fx-background-color: #E74C3C;");   // Red
                    toast.setStyle("-fx-text-fill: #721c24; -fx-background-color: #f8d7da; -fx-border-color: #f5c6cb; "
                            + "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                case WARNING ->
                    // toast.setStyle(toast.getStyle() + "-fx-background-color: #F1C40F; -fx-text-fill: black;"); // Yellow
                    toast.setStyle("-fx-text-fill: #856404; -fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; "
                            + "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
            }

            // Position at top-center of the toastPane
            Platform.runLater(() -> {
                toast.setMaxWidth(Double.MAX_VALUE);
                toast.setPrefWidth(toastPane.getWidth());
                toast.setAlignment(Pos.CENTER);
                toast.setLayoutX(0);
                toast.setLayoutY(10);
            });

            // toast.setLayoutX((toastPane.getWidth() - toast.getWidth()) / 2);
            // toast.setLayoutY(10);
            // Platform.runLater(() -> {
            //     toast.setLayoutX((toastPane.getWidth() - toast.getWidth()) / 2);
            //     toast.setLayoutY(10);
            // });
            toastPane.getChildren().add(toast);

            // Animate fade out after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
                Platform.runLater(() -> toastPane.getChildren().remove(toast));
            }).start();

            // toast.setOpacity(0);
            // // Add toast to the root (cameraView's parent)
            // StackPane root = (StackPane) cameraView.getParent();
            // root.getChildren().add(toast);
            // // Position at the top center
            // toast.setTranslateY(-cameraView.getFitHeight() / 2 + 30);
            // // Fade in
            // FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
            // fadeIn.setFromValue(0);
            // fadeIn.setToValue(1);
            // // Fade out after delay
            // FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
            // fadeOut.setFromValue(1);
            // fadeOut.setToValue(0);
            // fadeOut.setDelay(Duration.seconds(2));
            // fadeIn.play();
            // fadeIn.setOnFinished(e -> fadeOut.play());
            // fadeOut.setOnFinished(e -> root.getChildren().remove(toast));
        });
    }

    @Override
    public void onAttendanceMarked(AttendanceRecord r, String message) {
        // Platform.runLater(() -> statusLabel.setText("Marked: " + r.getStudent().getName() + " (" + r.getStatus() + ")"));
        // Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        // alert.setTitle("Confirm Delete All");
        // alert.setHeaderText("Delete ALL Sessions");
        // alert.setContentText("WARNING: This will permanently delete ALL " + sessionList.size() + " sessions!\n\n" +
        //         "This action cannot be undone. Are you absolutely sure?");

        // showToast("Marked: " + r.getStudent().getName() + " (" + r.getStatus() + ")", ToastType.SUCCESS);
        showToast(message, ToastType.SUCCESS);
    }

    // F_MA: modified by felicia handling marking attendance ##
    @Override
    public void onAttendanceAutoUpdated() {
        // return;
    }

    // F_MA: modified by felicia handling marking attendance
    public void onAttendanceNotMarked(AttendanceRecord r) {
        // Platform.runLater(() -> statusLabel.setText("Error marking attendance for " + r.getStudent().getName() + ". Please try again."));
        showToast("Error marking attendance for " + r.getStudent().getName() + " (Student Id: " + r.getStudent().getStudentId() + ")", ToastType.ERROR);
    }

    public void onAttendanceSkipped(AttendanceRecord r, String message) {
        // Platform.runLater(() -> statusLabel.setText("Error marking attendance for " + r.getStudent().getName() + ". Please try again."));
        showToast(message, ToastType.WARNING);
    }
}
