package com.smartattendance.service;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class FaceProcessingService {
    private final FaceDetectionService faceDetectionService;

    private static final int DEFAULT_FACE_WIDTH = 100;
    private static final int DEFAULT_FACE_HEIGHT = 100;

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
                    System.out.println("Failed to convert to grayscale");
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
                System.out.println("Failed to crop face");
                return new Mat();
            }

            // Step 3: Resize to target dimensions
            resizedFace = resizeImage(croppedFace, targetWidth, targetHeight);
            if (resizedFace.empty()) {
                System.out.println("Failed to resize face");
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
            System.err.println("Error preprocessing face: " + e.getMessage());
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
            // chore(), Harry: Throw a custom error or built in error here
            System.out.println("No image found!");
        }

        Mat gray = new Mat();

        // Transform grayScale
        Imgproc.cvtColor(colorImg, gray, Imgproc.COLOR_BGR2GRAY);

        return gray;
    }

    public Mat cropDetectedFace(Mat grayImg, Rect faceRect) {
        if (grayImg.empty() || faceRect.width <= 0 || faceRect.height <= 0) {
            System.out.println("Invalid image or face rectangle");
            return new Mat(); // Return empty Mat or just throw custom Error
        }

        // Ensure the rectangle is within image bounds
        if (faceRect.x < 0 || faceRect.y < 0 ||
                faceRect.x + faceRect.width > grayImg.cols() ||
                faceRect.y + faceRect.height > grayImg.rows()) {
            System.out.println("Face rectangle is out of image bounds");
            return new Mat(); // Return empty Mat or just throw custom Error
        }

        // Extract the face region
        Mat croppedFace = grayImg.submat(faceRect);

        return croppedFace.clone();
    }

    public Mat resizeImage(Mat image, int width, int height) {
        if (image.empty()) {
            System.out.println("No image found!");
            return new Mat();
        }

        Mat resized = new Mat();
        Size targetSize = new Size(width, height);
        Imgproc.resize(image, resized, targetSize);

        return resized;
    }

    // ------ Histogram Computation ------
    public Mat computeHistogram(Mat image) {
        if (image.empty()) {
            System.out.println("Cannot compute histogram for empty image");
            return new Mat();
        }

        Mat hist = new Mat();

        try {
            // Histogram parameters
            MatOfInt histSize = new MatOfInt(256);
            MatOfFloat ranges = new MatOfFloat(0f, 256f);
            MatOfInt channels = new MatOfInt(0);

            // Calculate histogram
            Imgproc.calcHist(List.of(image), channels, new Mat(), hist, histSize, ranges);

            // Normalize histogram to range [0, 1]
            Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);

            return hist;
        } catch (Exception e) {
            System.err.println("Error computing histogram: " + e.getMessage());
            if (!hist.empty()) {
                hist.release();
            }
            return new Mat();
        }

    }

    public Mat computeAverageHistogram(List<Mat> rawFaceImages) {
        if (rawFaceImages.isEmpty() || rawFaceImages == null) {
            return new Mat();
        }

        try {
            Mat sumHistogram = Mat.zeros(256, 1, CvType.CV_32F); // safer initialization
            int validCount = 0;

            for (Mat rawImage : rawFaceImages) {
                // Preprocess each face image
                Rect fullRect = new Rect(0, 0, rawImage.cols(), rawImage.rows());
                Mat preprocessedFace = preprocessFace(rawImage, fullRect, DEFAULT_FACE_WIDTH, DEFAULT_FACE_HEIGHT);

                // Compute histogram
                Mat hist = computeHistogram(preprocessedFace);
                if (!hist.empty()) {
                    validCount++;
                }
                if (hist.type() != CvType.CV_32F || !hist.size().equals(new Size(1, 256))) {
                    hist.convertTo(hist, CvType.CV_32F);
                    hist = hist.reshape(1, 256); // ensure column vector
                }
                Core.add(sumHistogram, hist, sumHistogram);
                hist.release();

                preprocessedFace.release();
            }

            if (validCount == 0) {
                System.out.println("No valid images to compute average histogram");
                sumHistogram.release();
                return new Mat();
            }

            // Compute average
            sumHistogram.convertTo(sumHistogram, sumHistogram.type(), 1.0 / validCount);
            Core.normalize(sumHistogram, sumHistogram, 0, 1, Core.NORM_MINMAX);

            System.out.println("Computed average histogram from " + validCount + " images");
            return sumHistogram;
        } catch (Exception e) {
            System.err.println("Error computing average histogram: " + e.getMessage());
            return new Mat();
        }
    }

}
