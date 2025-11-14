package com.smartattendance.util;

import org.opencv.videoio.VideoCapture;
import com.smartattendance.config.Config;
import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * Camera manager for camera/VideoCapture lifecycle
 * Ensures only one camera instance is used throughout the application
 * Created since camera did not get released unless manually closed and had to
 * end task from task manager
 *
 * @author Thiha Swan Htet
 */
public class CameraUtils {

    private static CameraUtils instance;
    private static final ApplicationLogger appLogger = ApplicationLogger.getInstance();
    private VideoCapture capture;
    private int cameraId;
    private boolean isOpened = false;

    /**
     * Custom constructor
     */
    private CameraUtils() {
        this.capture = new VideoCapture();
        this.cameraId = Integer.parseInt(Config.get("camera.index"));
    }

    /**
     * Get the instance of CameraUtils
     *
     * @return CameraUtils instance
     */
    public static synchronized CameraUtils getInstance() {
        if (instance == null) {
            instance = new CameraUtils();
        }
        return instance;
    }

    /**
     * Open the camera with the specified ID
     * If camera is already open, does nothing
     *
     * @param cameraId the camera device ID (default is 0)
     * @return true if camera opened successfully, false otherwise
     */
    public boolean openCamera(int cameraId) {
        if (isOpened) {
            appLogger.info("Camera is already open with ID: " + this.cameraId);
            return true;
        }

        this.cameraId = cameraId;
        this.capture.open(cameraId);

        if (this.capture.isOpened()) {
            this.isOpened = true;
            appLogger.info("Camera opened successfully with ID: " + cameraId);
            return true;
        } else {
            appLogger.error("Failed to open camera with ID: " + cameraId);
            return false;
        }
    }

    /**
     * Open camera with default ID (0)
     *
     * @return true if camera opened successfully, false otherwise
     */
    public boolean openCamera() {
        return openCamera(0);
    }

    /**
     * Close and release the camera
     */
    public void releaseCamera() {
        if (this.capture != null && this.capture.isOpened()) {
            this.capture.release();
            this.isOpened = false;
            appLogger.info("Camera released successfully");
        }
    }

    /**
     * Get the VideoCapture instance
     *
     * @return the VideoCapture object
     */
    public VideoCapture getCapture() {
        return this.capture;
    }

    /**
     * Check if camera is currently open
     *
     * @return true if camera is open, false otherwise
     */
    public boolean isCameraOpen() {
        return this.isOpened && this.capture.isOpened();
    }

    /**
     * Get the current camera ID
     *
     * @return the camera device ID
     */
    public int getCameraId() {
        return this.cameraId;
    }

    public static int[] getAvailableCameras(int maxIndex) {
        java.util.ArrayList<Integer> availableCameras = new java.util.ArrayList<>();

        for (int i = 0; i <= maxIndex; i++) {
            VideoCapture testCapture = new VideoCapture();
            testCapture.open(i);

            if (testCapture.isOpened()) {
                availableCameras.add(i);
                appLogger.info("Camera found at index: " + i);
                testCapture.release();
            }
        }

        // Convert ArrayList to int array
        int[] result = new int[availableCameras.size()];
        for (int i = 0; i < availableCameras.size(); i++) {
            result[i] = availableCameras.get(i);
        }

        appLogger.info("Total cameras detected: " + result.length);
        return result;
    }

    public static int[] getAvailableCameras() {
        return getAvailableCameras(10);
    }
}
