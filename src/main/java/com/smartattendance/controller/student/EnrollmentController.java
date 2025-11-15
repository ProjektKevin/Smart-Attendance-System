package com.smartattendance.controller.student;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;
import com.smartattendance.model.entity.AuthSession;
import com.smartattendance.service.FaceDetectionService;
import com.smartattendance.service.ImageService;
import com.smartattendance.util.CameraUtils;
import com.smartattendance.util.OpenCVUtils;
import com.smartattendance.util.security.log.ApplicationLogger;
import com.smartattendance.util.security.log.AttendanceLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Controller to connect the camera view from student enrollment and call
 * required services to perform face detection, and enrollment
 *
 * User captures faces → images saved to disk
 * Load all images, train using HistogramRecognizer and OpenFaceRecognizer
 * (compute average histogram and embedding)
 * Save average histogram and average embedding to face_data table in DB
 * Delete captured images folder to free disk space
 *
 * @author Thiha Swan Htet
 */
public class EnrollmentController {
	@FXML
	private Button button;
	@FXML
	private Button captureButton;
	@FXML
	private Button cancelButton;
	@FXML
	private Label statusLabel;
	@FXML
	private ImageView currentFrame;

	private ScheduledExecutorService timer;
	private ScheduledExecutorService captureTimer;
	private ExecutorService enrollmentExecutor;
	private boolean cameraActive = false;
	private boolean capturing = false;

	// Capture settings
	private static final int CAPTURE_INTERVAL_MS = 1000; // 1 second
	private AtomicInteger captureCount = new AtomicInteger(0);
	private static final int MAX_CAPTURES = Integer.parseInt(Config.get("enrollment.image.amount"));

	// Dependencies
	private final AuthSession session = ApplicationContext.getAuthSession();
	private final FaceDetectionService faceDetectionService = ApplicationContext.getFaceDetectionService();
	private final ImageService imageService = ApplicationContext.getImageService();
	private final CameraUtils cameraUtils = ApplicationContext.getCameraUtils();
	private final ApplicationLogger appLogger = ApplicationLogger.getInstance();
	private final AttendanceLogger attendanceLogger = AttendanceLogger.getInstance();

	/**
	 * Shared frame between the display thread and capture thread.
	 */
	private Mat sharedFrame = new Mat();

	/**
	 * Lock object that ensures only one thread can access sharedFrame at a time.
	 * Prevents race condition
	 */
	private final Object frameLock = new Object();

	/**
	 * Shared face detection results for both display and capture
	 */
	private MatOfRect cachedFaces = new MatOfRect();

	/**
	 * Initialize the controller - set initial status
	 */
	@FXML
	public void initialize() {
		statusLabel.setText("Status: Camera not started");
		captureButton.setDisable(true);
	}

	/**
	 * The action triggered by pushing the start camera button on the GUI
	 *
	 * @param event the start camera button event
	 *
	 */
	@FXML
	protected void startCamera(ActionEvent event) {
		if (!this.cameraActive) {
			// start the video capture using cameraUtils
			if (this.cameraUtils.openCamera()) {
				this.cameraActive = true;

				// grab a frame
				Runnable frameGrabber = new Runnable() {

					@Override
					public void run() {
						/*
						 * Read and process frame
						 * The called function detect faces and draw rectangles
						 * The detect faces got assigned to the shared ones and the same oes for the
						 * frame
						 */
						readAndProcessFrame();
					}
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

				// update the button content and status
				Platform.runLater(() -> {
					this.button.setText("Stop Camera");
					this.statusLabel.setText("Status: Camera Active");
					this.captureButton.setDisable(false);
				});
			} else {
				// log the error
				appLogger.error("Impossible to Open Camera Connection");
				Platform.runLater(() -> statusLabel.setText("Status: Camera Error"));
			}
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// Stop capture if it was running
			if (this.capturing) {
				stopCapture();
			}
			// update again the button content and status
			this.button.setText("Start Camera");
			this.statusLabel.setText("Status: Camera Stopped");
			this.captureButton.setDisable(true);

			// stop the timer
			this.stopAcquisition();
		}
	}

	/**
	 * Start capturing face images at 1-second intervals
	 * Capture and save images
	 * Captures when at least 1 face is detected (handles multiple face detections)
	 *
	 * @param event the capture button event
	 */
	@FXML
	protected void startCapture(ActionEvent event) {
		if (!this.capturing) {
			// Validate that we have a logged-in user
			if (session.getCurrentUser() == null) {
				statusLabel.setText("Status: Error - No user logged in");
				return;
			}

			this.capturing = true;
			this.captureCount.set(0);

			// Update UI
			Platform.runLater(() -> {
				this.captureButton.setText("Capturing...");
				this.captureButton.setDisable(true);
				this.statusLabel.setText("Status: Capturing faces (0/" + MAX_CAPTURES + ")");
			});

			// Create capture task that runs every 1 second
			Runnable captureTask = new Runnable() {
				@Override
				public void run() {
					appLogger
							.info("Starting Enrollment Capture for Student: " + session.getCurrentUser().getUserName());

					if (!capturing || captureCount.get() >= MAX_CAPTURES) {
						appLogger
								.info("Stopping Enrollment Capture for Student: "
										+ session.getCurrentUser().getUserName());
						stopCapture();
						return;
					}

					// Move synchronization BEFORE face count check to prevent race condition
					// This ensures the face count and frame capture are atomic
					synchronized (frameLock) {
						int faceCount = cachedFaces.toArray().length;

						// Capture face if there is only one person
						if (faceCount == 1) {
							// Save image and store metadata
							int studentId = session.getCurrentUser().getId();
							appLogger.info("Attempting to save image for student: " + studentId);
							if (imageService.captureAndSaveImage(studentId, sharedFrame)) {
								int count = captureCount.incrementAndGet();
								appLogger.info("Successfully captured " + count + " images for " + studentId);
								Platform.runLater(() -> {
									statusLabel.setText("Status: Capturing faces (" + count + "/" + MAX_CAPTURES + ")");
								});
							} else {
								appLogger.error("Failed to save image for student: " + studentId);
							}
						} else {
							appLogger.warn("No face detected in current frame");
							statusLabel.setText("Please ensure only 1 person is present");
						}
					}
				}
			};

			this.captureTimer = Executors.newSingleThreadScheduledExecutor();
			this.captureTimer.scheduleAtFixedRate(captureTask, 0, CAPTURE_INTERVAL_MS, TimeUnit.MILLISECONDS);
		} else {
			stopCapture();
		}
	}

	/**
	 * Stop capturing face images
	 * Train, Persist, and Cleanup
	 */
	private void stopCapture() {
		this.capturing = false;

		if (this.captureTimer != null && !this.captureTimer.isShutdown()) {
			try {
				this.captureTimer.shutdown();
				this.captureTimer.awaitTermination(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.err.println("Error stopping capture: " + e.getMessage());
			}
		}

		int count = captureCount.get();

		// Update UI
		Platform.runLater(() -> {
			this.captureButton.setText("Capture Faces");
			if (count > 0) {
				this.statusLabel
						.setText("Status: Captured " + count + " face image(s). Training and persisting enrollment...");

				// Trigger training, persistence, and cleanup on background thread
				if (enrollmentExecutor == null || enrollmentExecutor.isShutdown()) {
					enrollmentExecutor = Executors.newSingleThreadExecutor();
				}

				enrollmentExecutor.submit(() -> {
					boolean success = imageService.trainAndPersistEnrollment(session);

					// Update UI with final result
					Platform.runLater(() -> {
						if (success) {
							String studentName = session.getCurrentUser().getUserName();
							this.statusLabel.setText("Status: Enrollment complete for " + studentName);

							// Release camera before navigating away
							stopAcquisition();

							// Show info dialog
							showInfoAlert("Enrollment Successful",
									"Your facial data has been enrolled successfully. Click OK to proceed to the student portal.");

							// Navigate to student dashboard
							try {
								Parent studentRoot = FXMLLoader
										.load(getClass().getResource("/view/StudentRootView.fxml"));
								Stage stage = (Stage) statusLabel.getScene().getWindow();
								stage.setScene(new Scene(studentRoot));
								stage.setTitle("Student Portal");

								attendanceLogger
										.info("Enrollment Successful For " + studentName);
								appLogger
										.info("Navigating User to the Student Portal");
							} catch (Exception e) {
								this.statusLabel.setText("Error Loading Student Portal: " + e.getMessage());
								appLogger.error("Error Navigating to Student Portal", e);
								e.printStackTrace();
							}
						} else {
							this.statusLabel.setText("Status: Enrollment failed. Please try again.");
						}
					});
				});
			} else {
				this.statusLabel.setText("Status: No faces captured");
			}
		});
	}

	/**
	 * Read the frame from camera
	 * Detect faces in it
	 * Display it with rectangles to user
	 * Store the frame and detection results for capture thread to use
	 */
	private void readAndProcessFrame() {
		Mat frame = new Mat();

		// check if the capture is open using cameraUtils
		if (this.cameraUtils.isCameraOpen()) {
			try {
				// read the current frame
				this.cameraUtils.getCapture().read(frame);

				// if the frame is not empty, process it
				if (!frame.empty()) {
					// Detect faces
					MatOfRect faces = faceDetectionService.detectFaces(frame);

					/*
					 * Ensure only one thread can execute this block at a time
					 * If not synchronized, the capture thread might read sharedFrame while copying
					 * data
					 */
					synchronized (frameLock) {
						// Copy frame to shared buffer
						frame.copyTo(sharedFrame);
						// Store the face detection results
						cachedFaces = faces;
					}

					// Draw rectangles on the frame for display
					int faceCount = faceDetectionService.drawFaceRectangles(frame, faces);

					if (faceCount != 1) {
						statusLabel.setText("More than 1 Face(s) Detected");
						appLogger.warn("Expected 1 face, but got " + faceCount + " faces");
					}

					// Convert and show the frame to user
					Image imageToShow = OpenCVUtils.mat2Image(frame);
					updateImageView(this.currentFrame, imageToShow);
				}

			} catch (Exception e) {
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
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

		// Stop capture timer if running
		if (this.captureTimer != null && !this.captureTimer.isShutdown()) {
			try {
				this.captureTimer.shutdown();
				this.captureTimer.awaitTermination(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.err.println("Exception in stopping capture timer: " + e);
			}
		}

		// Stop enrollment executor if running
		if (this.enrollmentExecutor != null && !this.enrollmentExecutor.isShutdown()) {
			try {
				this.enrollmentExecutor.shutdown();
				this.enrollmentExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.err.println("Exception in stopping enrollment executor: " + e);
			}
		}

		// Release camera using cameraUtils
		this.cameraUtils.releaseCamera();
	}

	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 *
	 * @param view
	 *              the {@link ImageView} to update
	 * @param image
	 *              the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image) {
		OpenCVUtils.onFXThread(view.imageProperty(), image);
	}

	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed() {
		this.stopAcquisition();
	}

	/**
	 * Show an info alert dialog to the user
	 *
	 * @param title   the title of the alert
	 * @param message the message content
	 */
	private void showInfoAlert(String title, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
				javafx.scene.control.Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

}
