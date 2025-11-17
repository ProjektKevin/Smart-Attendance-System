
package com.smartattendance.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import org.opencv.core.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;
import com.smartattendance.model.enums.ToastType;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.FaceDetectionService;
import com.smartattendance.service.FaceRecognitionService;
import com.smartattendance.service.RecognitionObserver;
import com.smartattendance.service.recognition.RecognitionResult;
import com.smartattendance.util.CameraUtils;
import com.smartattendance.util.OpenCVUtils;
// // F_MA: modified by felicia handling marking attendance ##for testing
// import com.smartattendance.service.RecognitionServiceTest;
import com.smartattendance.util.security.log.ApplicationLogger;
import com.smartattendance.util.security.log.AttendanceLogger;

/**
 * Controller for managing face recognition and attendance logging and update.
 *
 * This controller handles:
 *      1. Opening camera and displaying live video.
 *      2. Detecting faces using selected model.
 *      3. Recognizing students, logging and update attendance automatically.
 *      4. Displaying UI updates, alerts and toast messages.
 * 
 * @author Min Thet Khine (initialize, startRecognition, stopRecognition, clearHistory, 
 *         grabFrame, stopAcquisition, updateImageView, processRecognitionResults, 
 *         logAttendance, logUnknownFace, updateFPS, loadSessionStudentsAsync)
 * @author Chue Wan Yan (create showToast, onAttendanceMarked, onAttenedanceNotMarked, 
 *         onAttendanceSkipped, requestUserCongirmationAsync. Added javadoc comments.)
 *
 * @version 13:23 15 Nov 2025
 *
 */
public class RecognitionController implements RecognitionObserver {
    @FXML
    private ImageView videoFeed; // Video feed display
    @FXML
    private Button startButton; // Start recognition button
    @FXML
    private Button captureButton; // Capture frame button
    @FXML
    private Button clearHistoryButton; // Clear recognition history button
    @FXML
    private Label statusLabel; // Label showing current status
    @FXML
    private Label currentStudentLabel; // Label showing current recognized student
    @FXML
    private Label confidenceLabel; // Label showing confidence score
    @FXML
    private Label cameraStatusLabel; // Label showing camera connection status
    @FXML
    private Label modelStatusLabel; // Label showing model load status
    @FXML
    private Label totalDetectionsLabel; // Label showing total detections
    @FXML
    private Label uniqueStudentsLabel; // Label showing unique students detected
    @FXML
    private Label fpsLabel; // Label showing FPS of the video feed
    // F_MA: added by felicia handling marking attendance
    @FXML
    private Pane toastPane; // Pane for displaying toast messages
    @FXML
    private ListView<String> recognitionListView; // List view showing recognition logs

    // ======= Services =======
    private FaceDetectionService faceDetectionService; // Service for face detection
    private FaceRecognitionService faceRecognitionService; // Service for face recognition
    private final CameraUtils cameraUtils = ApplicationContext.getCameraUtils(); // Camera utils
    // F_MA: added by felicia handling marking attendance
    private final AttendanceService attendanceService = ApplicationContext.getAttendanceService(); // Service for student attendance

    // ======= FXML UI Components =======
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
    private static final long RECOGNITION_COOLDOWN_MS = Long.parseLong(Config.get("cooldown.seconds")) * 1000; // * 1000 to change from seconds to milliseconds
    private static final long ALERT_COOLDOWN_MS = 5000; // 5 seconds

    // Time formatter for logs
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Loading state
    private boolean studentsLoaded = false;

    // Confidence threshold
    private static final double LOW_CONFIDENCE_THRESHOLD = 30.0;

    // Logger
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();
    private final AttendanceLogger attendanceLogger = AttendanceLogger.getInstance();

    // =======================================================================
    /**
     * Initializes the controller and sets up services and observers.
     */
    @FXML
    public void initialize() {
        // Register this controller with ApplicationContext
        ApplicationContext.setRecognitionController(this);
        
        // Add this controller to Attendance Service's observer
        attendanceService.addObserver(this);
        // Get services from ApplicationContext
        faceDetectionService = ApplicationContext.getFaceDetectionService();
        faceRecognitionService = ApplicationContext.getFaceRecognitionService();

        this.startButton.setText("Start Recognition");
        this.startButton.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");

        statusLabel.setText("Status: Loading...");
        cameraStatusLabel.setText("Camera: Disconnected");
        modelStatusLabel.setText("Model: Not Loaded");
    }

     /**
     * Starts or stops face recognition when the start button is clicked.
     */
    @FXML
    private void startRecognition() {
        appLogger.info("started loading student list");
        loadSessionStudentsAsync();
        appLogger.info("Loading done!");

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
                appLogger.error("Camera Connection Failed");
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

    /**
     * Stops recognition, releases the camera, and updates the UI.
     */
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

    /**
     * Clears the recognition history displayed in the list view.
     */
    @FXML
    private void clearHistory() {
        // Empty method stub for FXML
        recognitionListView.getItems().clear();
        appLogger.info("Clear history button clicked");
    }

    // ======= Helper Functions =======
    /**
     * Grabs a frame from the camera, detects and recognizes faces,
     * and updates the UI accordingly.
     *
     * @return OpenCV {@link Mat} frame
     */
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
                appLogger.error("Exception during the image elaboration: " + e);
            }
        }

        return frame;
    }

    /**
     * Stops the frame acquisition and releases camera.
     */
    public void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                appLogger.error("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        // Release camera using cameraUtils
        this.cameraUtils.releaseCamera();
    }

    /**
     * Updates the given ImageView with the provided image on the JavaFX thread.
     *
     * @param view  The ImageView to update
     * @param image The image to display
     */
    private void updateImageView(ImageView view, Image image) {
        OpenCVUtils.onFXThread(view.imageProperty(), image);
    }

    /**
     * Processes the recognition results for detected faces.
     *
     * @param results List of {@link RecognitionResult} objects
     */
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

    /**
     * Logs a recognized student's attendance and updates the UI.
     *
     * @param student    The recognized {@link Student}
     * @param confidence The recognition confidence
     */
    private void logAttendance(Student student, double confidence) {
        // Check if there's an active session
        Integer sessionId = ApplicationContext.getAuthSession().getActiveSessionId();
        if (sessionId == null) {
            appLogger.info("No active session - cannot mark attendance");
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
            appLogger.info("NEW STUDENT RECOGNIZED: " + logEntry);
        } else {
            appLogger.info("STUDENT RE-DETECTED: " + logEntry);
        }

        CompletableFuture.runAsync(() -> {
            try {
                // Get full session object
                Session session = ApplicationContext.getSessionService().findById(sessionId);
                if (session == null) {
                    appLogger.info("Session not found - cannot mark attendance");
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
                appLogger.info("calling mark attendance now");
                ApplicationContext.getAttendanceService().markAttendance(record);

                attendanceLogger.info("Attendance record created for: " + studentName);
            } catch (Exception e) {
                appLogger.error("Error creating attendance record: " + e.getMessage());
            }
        });
    }

    /**
     * Logs an unknown face detection and shows alerts.
     *
     * @param confidence The recognition confidence
     */
    private void logUnknownFace(double confidence) {
        appLogger.info("Unknown face detected with confidence: " + confidence);

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

        appLogger.info("UNKNOWN FACE: " + logEntry);
    }

    /**
     * Updates FPS display in the UI.
     */
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

     /**
     * Loads enrolled students for the current session asynchronously if any.
     */
    private void loadSessionStudentsAsync() {
        appLogger.info("Start loading session students asynchronously");
        new Thread(() -> {
            try {
                // Check if there's an active session
                if (!ApplicationContext.getAuthSession().hasActiveSession()) {
                    appLogger.info("No active session");
                    Platform.runLater(() -> {
                        modelStatusLabel.setText("Model: No Active Session");
                        statusLabel.setText("Status: Please start a session first");
                    });
                    return;
                }

                // Get the active session ID
                Integer sessionId = ApplicationContext.getAuthSession().getActiveSessionId();
                appLogger.info("Active session ID: " + sessionId);

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

                        appLogger.info("Successfully loaded " + studentCount + " students");
                    } else {
                        modelStatusLabel.setText("Model: No Students Found");
                        statusLabel.setText("Status: No enrolled students");

                        appLogger.info("No enrolled students found in database");
                    }
                });

            } catch (Exception e) {
                appLogger.error("Error loading students: " + e.getMessage());
                e.printStackTrace();

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    modelStatusLabel.setText("Model: Error");
                    statusLabel.setText("Status: Database Error");

                    appLogger.info("Failed to load session students from database");
                });
            }
        }).start();
    }

    /**
     * Displays a toast message in the UI.
     *
     * @param message The message to display
     * @param type    The {@link ToastType} (SUCCESS, ERROR, WARNING)
     */
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
                toast.setAlignment(Pos.CENTER_LEFT);
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
    
    // ======= RecognitionObserver Methods =======
    /**
     * Called when an attendance record is marked. Implementation required by
     * {@link RecognitionObserver}.
     *
     * @param message Optional message describing the marking
     */
    @Override
    public void onAttendanceMarked(String message) {
        // showToast("Marked: " + r.getStudent().getName() + " (" + r.getStatus() + ")",
        // ToastType.SUCCESS);
        showToast(message, ToastType.SUCCESS);
    }

    // F_MA: modified by felicia handling marking attendance ##
    // @Override
    // public void onAttendanceAutoUpdated() {
    //     // return;
    // }

    /**
     * Called when an attendance record not marked. Implementation required by
     * {@link RecognitionObserver}.
     *
     * @param message Optional message describing the marking
     */
    @Override
    public void onAttendanceNotMarked(AttendanceRecord r) {
        // Platform.runLater(() -> statusLabel.setText("Error marking attendance for " +
        // r.getStudent().getName() + ". Please try again."));
        showToast("Error marking attendance for " + r.getStudent().getName() + " (Student Id: "
                + r.getStudent().getStudentId() + ")", ToastType.ERROR);
    }

    /**
     * Called when skipping remark for attendance. Implementation required by
     * {@link RecognitionObserver}.
     *
     * @param message Optional message describing the marking
     */
    @Override
    public void onAttendanceSkipped(String message) {
        // Platform.runLater(() -> statusLabel.setText("Error marking attendance for " +
        // r.getStudent().getName() + ". Please try again."));
        showToast(message, ToastType.WARNING);
    }

    /**
     * Shows a confirmation dialog asynchronously for low-confidence recognition.
     *
     * @param record   The attendance record requiring confirmation
     * @param callback Consumer<Boolean> that receives true if confirmed, false otherwise
     */
    // Added by F_MA: felicia handling attendance marking
    public void requestUserConfirmationAsync(AttendanceRecord record, Consumer<Boolean> callback) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Student Identity");
            alert.setHeaderText("Low Confidence Detection");
            alert.setContentText(String.format(
                    "Detected student:\n\nName: %s\nID: %d\n\nIs this correct?",
                    record.getStudent().getName(),
                    record.getStudent().getStudentId()
            ));

            ButtonType yesButton = new ButtonType("Yes");
            ButtonType noButton = new ButtonType("No");
            alert.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = alert.showAndWait();
            callback.accept(result.isPresent() && result.get() == yesButton);
        });
    }

}