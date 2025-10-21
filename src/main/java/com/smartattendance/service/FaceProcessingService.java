package com.smartattendance.service;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class FaceProcessingService {
    private final FaceDetectionService faceDetectionService;

    public FaceProcessingService(FaceDetectionService faceDetectionService) {
        this.faceDetectionService = faceDetectionService;
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
        }

        Mat gray = new Mat();

        // Transform grayScale
        Imgproc.cvtColor(colorImg, gray, Imgproc.COLOR_BGR2GRAY);

        return gray;
    }
}
