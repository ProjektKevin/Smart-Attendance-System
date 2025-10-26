package com.smartattendance.controller.student;

import com.smartattendance.ApplicationContext;
import com.smartattendance.service.FaceDetectionService;
import com.smartattendance.util.OpenCVUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.videoio.VideoCapture;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller to connect the camera view from student enrollment and call
 * required services to perform face detection, and enrollment
 * 
 */
public class EnrollmentController {
	@FXML
	private Button button;
	@FXML
	private ImageView currentFrame;

	private ScheduledExecutorService timer;
	private VideoCapture capture = new VideoCapture();
	private boolean cameraActive = false;
	private static int cameraId = 0;

	// the detection service to call detect faces
	private final FaceDetectionService faceDetectionService = ApplicationContext.getFaceDetectionService();

	/**
	 * The action triggered by pushing the start camera button on the GUI
	 *
	 * @param event the start camera button event
	 * 
	 */
	@FXML
	protected void startCamera(ActionEvent event) {
		if (!this.cameraActive) {
			// start the video capture
			this.capture.open(cameraId);

			// is the video stream available?
			if (this.capture.isOpened()) {
				this.cameraActive = true;

				// grab a frame
				Runnable frameGrabber = new Runnable() {

					@Override
					public void run() {
						// Process a single frame
						Mat frame = grabFrame();
						// Convert and show the frame
						Image imageToShow = OpenCVUtils.mat2Image(frame);
						updateImageView(currentFrame, imageToShow);
					}
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

				// update the button content
				this.button.setText("Stop Camera");
			} else {
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.button.setText("Start Camera");

			// stop the timer
			this.stopAcquisition();
		}
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Mat} to show
	 */
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
					int faceCount = OpenCVUtils.drawFaceRectangles(frame, faces);

					if(faceCount != 1){
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

		if (this.capture.isOpened()) {
			// release the camera
			this.capture.release();
		}
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

}