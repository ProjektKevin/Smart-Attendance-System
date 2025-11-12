
package com.smartattendance.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

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
import java.util.concurrent.CompletableFuture;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;
import com.smartattendance.model.enums.ToastType;
import com.smartattendance.service.AttendanceObserver;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.FaceDetectionService;
import com.smartattendance.service.FaceRecognitionService;
import com.smartattendance.service.recognition.RecognitionResult;
import com.smartattendance.util.CameraUtils;
import com.smartattendance.util.OpenCVUtils;
// F_MA: modified by felicia handling marking attendance ##for testing
import com.smartattendance.service.RecognitionServiceTest;

public class RecognitionController implements AttendanceObserver {
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
    // F_MA: added by felicia handling marking attendance
    @FXML
    private Pane toastPane;
    @FXML
    private ListView<String> recognitionListView;
    // =======================================================================
    private FaceDetectionService faceDetectionService;
    private FaceRecognitionService faceRecognitionService;
    private final CameraUtils cameraUtils = ApplicationContext.getCameraUtils();
    // F_MA: added by felicia handling marking attendance
    private final AttendanceService attendanceService = ApplicationContext.getAttendanceService();

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
    private long lastAlertTime = 0;
    private static final long RECOGNITION_COOLDOWN_MS = 10000; // 10 seconds
    private static final long ALERT_COOLDOWN_MS = 5000; // 5 seconds

    // Time formatter for logs
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Loading state
    private boolean studentsLoaded = false;

    private static final double LOW_CONFIDENCE_THRESHOLD = 30.0;

    // F_MA: modified by felicia handling marking attendance ##for testing
    private RecognitionServiceTest testService;

    // =======================================================================
    @FXML
    public void initialize() {
        // Add this controller to Attendance Service's observer
        attendanceService.addObserver(this);
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

        // Check parametes

        // Safety Camera release
        if (this.cameraActive) {
            this.stopAcquisition();
            this.cameraUtils.releaseCamera();

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
                    if (confidence >= LOW_CONFIDENCE_THRESHOLD) {
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

        CompletableFuture.runAsync(() -> {
            try {
                // Get full session object
                Session session = ApplicationContext.getSessionService().findById(sessionId);
                if (session == null) {
                    System.out.println("Session not found - cannot mark attendance");
                    return;
                }

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
        });
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

            // // Show that the person is unrecognized
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAlertTime > ALERT_COOLDOWN_MS) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Unknown Face Detected");
                alert.setHeaderText("Unrecognized Person");
                alert.setContentText(String.format(
                        "An unknown face was detected with %.1f%% confidence.\n\nThis person is not enrolled in the system.",
                        confidence));
                alert.show();
                lastAlertTime = currentTime;
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
                    // toast.setStyle(toast.getStyle() + "-fx-background-color: #E74C3C;"); // Red
                    toast.setStyle("-fx-text-fill: #721c24; -fx-background-color: #f8d7da; -fx-border-color: #f5c6cb; "
                            + "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                case WARNING ->
                    // toast.setStyle(toast.getStyle() + "-fx-background-color: #F1C40F;
                    // -fx-text-fill: black;"); // Yellow
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
            // toast.setLayoutX((toastPane.getWidth() - toast.getWidth()) / 2);
            // toast.setLayoutY(10);
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
        // Platform.runLater(() -> statusLabel.setText("Marked: " +
        // r.getStudent().getName() + " (" + r.getStatus() + ")"));
        // Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        // alert.setTitle("Confirm Delete All");
        // alert.setHeaderText("Delete ALL Sessions");
        // alert.setContentText("WARNING: This will permanently delete ALL " +
        // sessionList.size() + " sessions!\n\n" +
        // "This action cannot be undone. Are you absolutely sure?");

        // showToast("Marked: " + r.getStudent().getName() + " (" + r.getStatus() + ")",
        // ToastType.SUCCESS);
        showToast(message, ToastType.SUCCESS);
    }

    // F_MA: modified by felicia handling marking attendance ##
    @Override
    public void onAttendanceAutoUpdated() {
        // return;
    }

    // F_MA: modified by felicia handling marking attendance
    public void onAttendanceNotMarked(AttendanceRecord r) {
        // Platform.runLater(() -> statusLabel.setText("Error marking attendance for " +
        // r.getStudent().getName() + ". Please try again."));
        showToast("Error marking attendance for " + r.getStudent().getName() + " (Student Id: "
                + r.getStudent().getStudentId() + ")", ToastType.ERROR);
    }

    public void onAttendanceSkipped(AttendanceRecord r, String message) {
        // Platform.runLater(() -> statusLabel.setText("Error marking attendance for " +
        // r.getStudent().getName() + ". Please try again."));
        showToast(message, ToastType.WARNING);
    }

}