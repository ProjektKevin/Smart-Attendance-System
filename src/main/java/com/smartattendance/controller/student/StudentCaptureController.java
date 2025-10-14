package com.smartattendance.controller.student;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_videoio;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class StudentCaptureController {

    @FXML private ImageView cameraView;
    @FXML private Button startBtn;
    @FXML private Button stopBtn;
    @FXML private Button captureBtn;
    @FXML private Label statusLabel;

    private VideoCapture capture;
    private ScheduledExecutorService timer;
    private volatile boolean cameraActive = false;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @FXML
    private void startCamera() {
        if (cameraActive) return;

        capture = new VideoCapture(0);
        capture.set(opencv_videoio.CAP_PROP_FRAME_WIDTH, 1280);
        capture.set(opencv_videoio.CAP_PROP_FRAME_HEIGHT, 720);

        if (!capture.isOpened()) {
            statusLabel.setText("Cannot open camera.");
            return;
        }

        cameraActive = true;
        startBtn.setDisable(true);
        stopBtn.setDisable(false);
        captureBtn.setDisable(false);
        statusLabel.setText("Camera running...");

        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(() -> {
            if (!cameraActive) return;
            Mat frame = new Mat();
            if (capture.read(frame) && !frame.empty()) {
                var img = OpenCvFxUtils.mat2Image(frame);
                Platform.runLater(() -> cameraView.setImage(img));
            }
        }, 0, 33, TimeUnit.MILLISECONDS);
    }

    @FXML
    private void stopCamera() {
        if (!cameraActive) return;
        cameraActive = false;

        if (timer != null) {
            timer.shutdownNow();
            timer = null;
        }
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        startBtn.setDisable(false);
        stopBtn.setDisable(true);
        captureBtn.setDisable(true);
        statusLabel.setText("Camera stopped");
    }

    @FXML
    private void capture() {
        if (!cameraActive || capture == null || !capture.isOpened()) return;

        Mat frame = new Mat();
        if (capture.read(frame) && !frame.empty()) {
            String studentId = StudentSessionContext.getCurrentStudentId();
            if (studentId == null || studentId.isBlank()) {
                statusLabel.setText("No student session. Please re-login.");
                return;
            }
            try {
                Path outDir = Path.of("data", "captures", studentId);
                Files.createDirectories(outDir);
                String filename = "img_" + LocalDateTime.now().format(fmt) + ".png";
                Path path = outDir.resolve(filename);

                opencv_imgcodecs.imwrite(path.toString(), frame);
                statusLabel.setText("Saved: " + path);
            } catch (Exception e) {
                statusLabel.setText("Failed to save image.");
                e.printStackTrace();
            }
        }
    }

    // Optional: if you hook controller lifecycle, call stopCamera() on close.
}
