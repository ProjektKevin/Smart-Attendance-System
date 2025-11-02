package com.smartattendance.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.smartattendance.ApplicationContext;
import com.smartattendance.service.FaceDetectionService;
import com.smartattendance.service.FaceRecognitionService;
import com.smartattendance.util.OpenCVUtils;

public class RecognitionController {
    @FXML
    private ImageView videoFeed;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button captureButton;
    @FXML
    private Button clearHistoryButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Label currentStudentLabel;
    @FXML
    private Label confidenceLabel;
    @FXML
    private Label cameraStatusLabel;
    @FXML
    private Label modelStatusLabel;
    @FXML
    private Label totalDetectionsLabel;
    @FXML
    private Label uniqueStudentsLabel;
    @FXML
    private Label fpsLabel;
    @FXML
    private ListView<String> recognitionListView;
    // =======================================================================
    private final FaceDetectionService faceDetectionService = ApplicationContext.getFaceDetectionService();
    private final FaceRecognitionService faceRecognitionService = ApplicationContext.getFaceRecognitionService();

    // OpenCV objects
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;
    private ScheduledExecutorService timer;
    private static int cameraId = 0;

    // =======================================================================
    @FXML
    public void initialize() {
        statusLabel.setText("Status: Ready");
        cameraStatusLabel.setText("Camera: Disconnected");
        modelStatusLabel.setText("Model: Not Loaded");
    }

    @FXML
    private void startRecognition() {
        if (!this.cameraActive) {
            this.capture.open(cameraId);

            // Check if camera opened successfully
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // Create a task to grab and process frames continuously
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        // STEP 1: Grab a frame from camera
                        Mat frame = grabFrame();

                        // STEP 2: Convert frame to JavaFX Image
                        Image imageToShow = OpenCVUtils.mat2Image(frame);

                        // STEP 3: Update the UI with the new frame
                        updateImageView(videoFeed, imageToShow);
                    }
                };

                // Schedule the frame grabber to run every 33ms (~30 FPS)
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // Update UI
                this.startButton.setText("Stop Recognition");
                this.cameraStatusLabel.setText("Camera: Connected");
                this.statusLabel.setText("Status: Recognition Active");

            } else {
                // Camera failed to open
                System.err.println("Camera Connection Failed");
                this.statusLabel.setText("Status: Camera Error");
            }

        } else {
            this.cameraActive = false;
            this.startButton.setText("Start Recognition");
            this.statusLabel.setText("Status: Stopped");

            // Stop frame grabbing and release camera
            this.stopAcquisition();
        }
    }

    @FXML
    private void stopRecognition() {
        statusLabel.setText("Status: Stopped");

        // Stop the timer
        if (timer != null && !timer.isShutdown()) {
            try {
                timer.shutdown();
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Release camera
        if (camera != null && camera.isOpened()) {
            camera.release();
        }

        cameraStatusLabel.setText("Camera: Disconnected");

        // Enable/disable buttons
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    // @FXML
    // private void captureFrame() {
    // // Empty method stub for FXML
    // System.out.println("Capture button clicked");
    // }

    @FXML
    private void clearHistory() {
        // Empty method stub for FXML
        recognitionListView.getItems().clear();
        System.out.println("Clear history button clicked");
    }

    private void startCameraFeed() {
        Runnable frameGrabber = () -> {
            Mat frame = new Mat();

            if (camera != null && camera.isOpened()) {
                camera.read(frame);

                if (!frame.empty()) {
                    // Convert Mat to Image and display
                    Image imageToShow = mat2Image(frame);
                    Platform.runLater(() -> videoFeed.setImage(imageToShow));
                }
            }
        };

        // Grab frames at ~30 FPS
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
    }

    private Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        org.opencv.imgcodecs.Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }


    // ----- Helper Functions -----
    private Mat grabFrame() {
		// init everything
		Mat frame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				// if the frame is not empty, process it
				if (!frame.empty()) {
					MatOfRect faces = faceDetectionService.detectFaces(frame);

					// Draw rectangles on the COLOR frame (not grayscale)
					int faceCount = faceDetectionService.drawFaceRectangles(frame, faces);

					if (faceCount != 1) {
						System.out.println("Too many people for enrollment");
						// chore(), Harry: Add validation here
					}
				}

			} catch (Exception e) {
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}

		return frame;
	}

    private void stopAcquisition() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (this.capture.isOpened()) {
			// release the camera
			this.capture.release();
		}
	}
}