package com.smartattendance.service;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * Process facial data
 * Crop faces, grayscale, ...etc
 * 
 * @author Min Thet Khine, Thiha Swan Htet
 */
public class FaceProcessingService {
    private final FaceDetectionService faceDetectionService;

    // Logger
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    // ===== Constructor =====
    public FaceProcessingService(FaceDetectionService faceDetectionService) {
        this.faceDetectionService = faceDetectionService;
    }

    // ===== Main Function =====
    public Mat preprocessFace(Mat faceImg, Rect faceRect, int targetWidth, int targetHeight) {
        return preprocessFace(faceImg, faceRect, targetWidth, targetHeight, true);
    }

    public Mat preprocessFace(Mat faceImg, Rect faceRect, int targetWidth, int targetHeight,
            boolean isProcessingHistogram) {
        Mat processedImg = null;
        Mat croppedFace = null;
        Mat resizedFace = null;

        try {
            // Step 1: Convert to grayscale or correct the color channel
            if (isProcessingHistogram) {
                processedImg = convertToGrayscale(faceImg);
                if (processedImg.empty()) {
                    appLogger.error("Failed to convert to grayscale");
                    return new Mat();
                }
            } else {
                processedImg = new Mat();

                // Need to correct the color channels (needed format: RGB)
                switch (faceImg.channels()) {
                    case 1:
                        Imgproc.cvtColor(faceImg, processedImg, Imgproc.COLOR_GRAY2RGB);
                        break;
                    case 3:
                        Imgproc.cvtColor(faceImg, processedImg, Imgproc.COLOR_BGR2RGB);
                        break;
                    case 4:
                        Imgproc.cvtColor(faceImg, processedImg, Imgproc.COLOR_BGRA2RGB);
                        break;
                    default:
                        processedImg = faceImg.clone();
                }

            }

            // Step 2: Crop the face region
            croppedFace = cropDetectedFace(processedImg, faceRect);
            if (croppedFace.empty()) {
                appLogger.error("Failed to crop face");
                return new Mat();
            }

            // Step 3: Resize to target dimensions
            resizedFace = resizeImage(croppedFace, targetWidth, targetHeight);
            if (resizedFace.empty()) {
                appLogger.error("Failed to resize face");
                return new Mat();
            }

            // Step 4: Apply histogram equalization for lighting normalization
            if (isProcessingHistogram) {
                Mat equalizedFace = new Mat();
                Imgproc.equalizeHist(resizedFace, equalizedFace);
                resizedFace.release();
                return equalizedFace;
            }

            return resizedFace;

        } catch (Exception e) {
            appLogger.error("Error preprocessing face: " + e.getMessage());
            return new Mat();

        } finally {
            // Clean up intermediate Mats
            if (processedImg != null && !processedImg.empty()) {
                processedImg.release();
            }
            if (croppedFace != null && !croppedFace.empty()) {
                croppedFace.release();
            }
        }
    }

    /**
     * Convert a color image to grayscale.
     * Face detection works better on grayscale images.
     * 
     * @param colorImg The input color image (BGR format from OpenCV)
     * @return A new Mat containing the grayscale version
     */
    public Mat convertToGrayscale(Mat colorImg) {
        if (colorImg.empty() || colorImg == null) {
            appLogger.error("No image found!");
        }

        Mat gray = new Mat();

        // Transform grayScale
        Imgproc.cvtColor(colorImg, gray, Imgproc.COLOR_BGR2GRAY);

        return gray;
    }

    public Mat cropDetectedFace(Mat grayImg, Rect faceRect) {
        if (grayImg.empty() || faceRect.width <= 0 || faceRect.height <= 0) {
            appLogger.error("Invalid image or face rectangle");
            return new Mat(); // Return empty Mat or just throw custom Error
        }

        // Ensure the rectangle is within image bounds
        if (faceRect.x < 0 || faceRect.y < 0 ||
                faceRect.x + faceRect.width > grayImg.cols() ||
                faceRect.y + faceRect.height > grayImg.rows()) {
            appLogger.warn("Face rectangle is out of image bounds");
            return new Mat(); // Return empty Mat or just throw custom Error
        }

        // Extract the face region
        Mat croppedFace = grayImg.submat(faceRect);

        return croppedFace.clone();
    }

    public Mat resizeImage(Mat image, int width, int height) {
        if (image.empty()) {
            appLogger.error("No image found!");
            return new Mat();
        }

        Mat resized = new Mat();
        Size targetSize = new Size(width, height);
        Imgproc.resize(image, resized, targetSize);

        return resized;
    }

}
