
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;
import com.smartattendance.service.FaceDetectionService;
import com.smartattendance.service.FaceRecognitionService;
import com.smartattendance.service.recognition.RecognitionResult;
import com.smartattendance.util.CameraUtils;
import com.smartattendance.util.OpenCVUtils;

public class RecognitionController {
    @FXML
    private ImageView videoFeed;
    @FXML
    private Button startButton;
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
    private FaceDetectionService faceDetectionService;
    private FaceRecognitionService faceRecognitionService;
    private final CameraUtils cameraUtils = ApplicationContext.getCameraUtils();

    // OpenCV objects
    private boolean cameraActive = false;
    private ScheduledExecutorService timer;

    // Recognition tracking
    private Set<Integer> recognizedStudentIds = new HashSet<>();
    private int totalDetections = 0;
    private long lastFrameTime = 0;
    private double currentFps = 0.0;

    // Recognition cooldown to avoid spam
    private long lastRecognitionTime = 0;
    private static final long RECOGNITION_COOLDOWN_MS = 3000; // 3 seconds

    // Time formatter for logs
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Loading state
    private boolean studentsLoaded = false;

    private static final double UNKNOWN_THRESHOLD = 30.0;

    // =======================================================================
    @FXML
    public void initialize() {
        // Get services from ApplicationContext
        faceDetectionService = ApplicationContext.getFaceDetectionService();
        faceRecognitionService = ApplicationContext.getFaceRecognitionService();

        statusLabel.setText("Status: Loading...");
        cameraStatusLabel.setText("Camera: Disconnected");
        modelStatusLabel.setText("Model: Not Loaded");
    }

    @FXML
    private void startRecognition() {
        System.out.println("started loading student list");
        loadSessionStudentsAsync();
        System.out.println("Loading done!");

        // Safety Camera release
        if (this.cameraActive) {
            this.stopAcquisition();
            this.cameraUtils.releaseCamera();;
        }

        if (!this.cameraActive) {
            // Open camera using CameraUtils
            if (this.cameraUtils.openCamera()) {
                this.cameraActive = true;

                // Create a task to grab and process frames continuously
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        // Check if camera is still active before processing
                        if (!cameraActive) {
                            return;
                        }

                        // STEP 1: Grab a frame from camera
                        Mat frame = grabFrame();

                        // STEP 2: Convert frame to JavaFX Image
                        Image imageToShow = OpenCVUtils.mat2Image(frame);

                        // STEP 3: Update the UI with the new frame
                        updateImageView(videoFeed, imageToShow);

                        // STEP 4: Update FPS display
                        updateFPS();
                    }
                };

                // Schedule the frame grabber to run every 33ms (~30 FPS)
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // Update UI
                Platform.runLater(() -> {
                    this.startButton.setText("Stop Recognition");
                    this.startButton.setStyle(
                            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
                    cameraStatusLabel.setText("Camera: Connected");
                    statusLabel.setText("Status: Recognition Active");
                });

            } else {
                // Camera failed to open
                System.err.println("Camera Connection Failed");
                statusLabel.setText("Status: Camera Error");
            }

        } else {
            this.cameraActive = false;
            this.startButton.setText("Start Recognition");
            this.startButton.setStyle(
                    "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
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

        // Release camera using CameraUtils
        this.cameraUtils.releaseCamera();
        this.cameraActive = false;

        cameraStatusLabel.setText("Camera: Disconnected");

        // Enable/disable buttons
        startButton.setDisable(false);
    }

    @FXML
    private void clearHistory() {
        // Empty method stub for FXML
        recognitionListView.getItems().clear();
        System.out.println("Clear history button clicked");
    }

    // ----- Helper Functions -----
    private Mat grabFrame() {
        Mat frame = new Mat();

        // check if the capture is open using cameraUtils
        if (this.cameraUtils.isCameraOpen()) {
            try {
                // read the current frame
                this.cameraUtils.getCapture().read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {
                    // 1. Detect Faces
                    MatOfRect faces = faceDetectionService.detectFaces(frame);
                    Rect[] facesArray = faces.toArray();

                    if (facesArray.length > 0) {
                        // Step 2: Extractface ROTs
                        List<Mat> faceROIs = faceRecognitionService.extractFaceROIs(frame, facesArray);

                        // Step 3: Recognize Faces
                        List<RecognitionResult> results = faceRecognitionService.recognizeFaces(faceROIs);

                        // Step 4: Process results
                        processRecognitionResults(results);

                        // Step 5: Draw rectangles and labels
                        faceDetectionService.drawFaceRectanglesWithLabels(frame, faces, results);

                        // Step 6: Update UI
                        final int faceCount = facesArray.length;
                        Platform.runLater(() -> {
                            totalDetectionsLabel.setText("Detections: " + faceCount);
                            statusLabel.setText("Status: Detected " + faceCount + " face(s)");
                        });
                    } else {
                        Platform.runLater(() -> statusLabel.setText("Status: No faces detected"));
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

        // Release camera using cameraUtils
        this.cameraUtils.releaseCamera();
    }

    private void updateImageView(ImageView view, Image image) {
        OpenCVUtils.onFXThread(view.imageProperty(), image);
    }

    private void processRecognitionResults(List<RecognitionResult> results) {
        long currentTime = System.currentTimeMillis();

        for (int i = 0; i < results.size(); i++) {
            RecognitionResult result = results.get(i);

            if (result.isMatch()) {
                Student student = result.getMatchedStudent();
                double confidence = result.getConfidenceScore();

                // Update current recognition display
                final String studentName = student.getName();
                final double finalConfidence = confidence;

                Platform.runLater(() -> {
                    currentStudentLabel.setText("Current: " + studentName);
                    confidenceLabel.setText(String.format("Confidence: %.1f%%", finalConfidence));
                });

                // Log attendance if cooldown period has passed
                if (currentTime - lastRecognitionTime > RECOGNITION_COOLDOWN_MS) {
                    // Handle based on confidence
                    if (confidence >= UNKNOWN_THRESHOLD) {
                        // Recognized face (confidence >= 30%)
                        logAttendance(student, confidence);
                    } else {
                        // Unknown face (confidence < 30%)
                        logUnknownFace(confidence);
                    }

                    lastRecognitionTime = currentTime;
                }

            }
        }

    }

    // Show attendance log in the UI
    private void logAttendance(Student student, double confidence) {
        // Check if there's an active session
        Integer sessionId = ApplicationContext.getAuthSession().getActiveSessionId();
        if (sessionId == null) {
            System.out.println("No active session - cannot mark attendance");
            return;
        }

        // Get full session object
        Session session = ApplicationContext.getSessionService().findById(sessionId);
        if (session == null) {
            System.out.println("Session not found - cannot mark attendance");
            return;
        }

        int studentId = student.getStudentId();
        String studentName = student.getName();

        // Track unique students
        boolean isNewStudent = recognizedStudentIds.add(studentId);
        totalDetections++;

        // Create log entry
        String timestamp = LocalDateTime.now().format(timeFormatter);

        String logEntry = String.format("[%s] %s (ID: %s) - %.1f%%",
                timestamp, studentName, studentId, confidence);

        final int uniqueCount = recognizedStudentIds.size();

        try {
            AttendanceRecord record = new AttendanceRecord(
                    student,
                    session,
                    AttendanceStatus.PRESENT, // For now, always mark as PRESENT
                    confidence,
                    MarkMethod.AUTO,
                    LocalDateTime.now());

            // Pass to attendance service
            System.out.println("calling mark attendance now");
            ApplicationContext.getAttendanceService().markAttendance(record);

            System.out.println("Attendance record created for: " + studentName);
        } catch (Exception e) {
            System.err.println("Error creating attendance record: " + e.getMessage());
        }

        // Update UI
        Platform.runLater(() -> {
            // Add to list view
            recognitionListView.getItems().add(0, logEntry);

            // Update counters
            uniqueStudentsLabel.setText("Unique: " + uniqueCount);

            // Keep list manageable (max 50 entries)
            if (recognitionListView.getItems().size() > 50) {
                recognitionListView.getItems().remove(50);
            }
        });

        // Console log
        if (isNewStudent) {
            System.out.println("NEW STUDENT RECOGNIZED: " + logEntry);
        } else {
            System.out.println("STUDENT RE-DETECTED: " + logEntry);
        }
    }

    private void logUnknownFace(double confidence) {
        System.out.println("Unknown face detected with confidence: " + confidence);

        totalDetections++;

        String timestamp = LocalDateTime.now().format(timeFormatter);
        String logEntry = String.format("[%s] Unknown Face Detected! (%.1f%%)", timestamp, confidence);

        Platform.runLater(() -> {
            currentStudentLabel.setText("Current: Unknown");
            confidenceLabel.setText(String.format("Confidence: %.1f%%", confidence));

            recognitionListView.getItems().add(0, logEntry);

            if (recognitionListView.getItems().size() > 50) {
                recognitionListView.getItems().remove(50);
            }
        });

        System.out.println("UNKNOWN FACE: " + logEntry);
    }

    // Update FPS display
    private void updateFPS() {
        long currentTime = System.currentTimeMillis();

        if (lastFrameTime > 0) {
            long frameDuration = currentTime - lastFrameTime;
            if (frameDuration > 0) {
                currentFps = 1000.0 / frameDuration;

                Platform.runLater(() -> {
                    if (fpsLabel != null) {
                        fpsLabel.setText(String.format("FPS: %.1f", currentFps));
                    }
                });
            }
        }

        lastFrameTime = currentTime;
    }

    // load enrolled students from session (if any)
    private void loadSessionStudentsAsync() {
        System.out.println("Start loading session students asynchronously");
        new Thread(() -> {
            try {
                // Check if there's an active session
                if (!ApplicationContext.getAuthSession().hasActiveSession()) {
                    System.out.println("No active session");
                    Platform.runLater(() -> {
                        modelStatusLabel.setText("Model: No Active Session");
                        statusLabel.setText("Status: Please start a session first");
                    });
                    return;
                }

                // Get the active session ID
                Integer sessionId = ApplicationContext.getAuthSession().getActiveSessionId();
                System.out.println("Active session ID: " + sessionId);

                // Get full session object from DB
                Session activeSession = ApplicationContext.getSessionService().findById(sessionId);
                if (activeSession == null) {
                    System.out.println("Session not found in database");
                    Platform.runLater(() -> {
                        modelStatusLabel.setText("Model: Session Not Found");
                        statusLabel.setText("Status: Invalid session");
                    });
                    return;
                }

                // Load students from database
                int studentCount = faceRecognitionService.loadEnrolledStudentsBySessionId(sessionId);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    if (studentCount > 0) {
                        studentsLoaded = true;
                        modelStatusLabel.setText("Model: Loaded (" + studentCount + " students)");
                        statusLabel.setText("Status: Ready");

                        if (startButton != null) {
                            startButton.setDisable(false);
                        }

                        System.out.println("Successfully loaded " + studentCount + " students");
                    } else {
                        modelStatusLabel.setText("Model: No Students Found");
                        statusLabel.setText("Status: No enrolled students");

                        System.out.println("No enrolled students found in database");
                    }
                });

            } catch (Exception e) {
                System.err.println("Error loading students: " + e.getMessage());
                e.printStackTrace();

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    modelStatusLabel.setText("Model: Error");
                    statusLabel.setText("Status: Database Error");

                    System.out.println("Failed to load session students from database");
                });
            }
        }).start();
    }
}