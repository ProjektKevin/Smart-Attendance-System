package com.smartattendance.service;

import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.core.Mat;

public class FaceDetectionService {
    private final CascadeClassifier faceDetector;

    // Default detection parameters
    private static final double DEFAULT_SCALE_FACTOR = 1.1;
    private static final int DEFAULT_MIN_NEIGHBORS = 3;
    private static final int DEFAULT_MIN_SIZE = 30;

    /**
     * Custom constructor which loads the cascadepath for either the detection or
     * the recognition model
     * 
     * @param cascadePath The cascadePath file path for haarcascades
     * @throws Exception If face detector is empty, filepath is wrong
     */
    public FaceDetectionService(String cascadePath) {
        // Load face detector
        this.faceDetector = new CascadeClassifier(cascadePath);
        if (faceDetector.empty()) {
            // chore(), Harry: Add logger to notify the loading error
            System.out.println("Error loading cascade file: " + cascadePath);
            // chore(), Harry: Throw a custom error or built in error here
        }
    }

    /**
     * Detect faces with custom parameters.
     * 
     * @param gray         The grayscale image to process
     * @param scaleFactor  How much to reduce image size at each scale (1.05-1.4)
     * @param minNeighbors How many neighbors each candidate should have (3-6)
     * @return MatOfRect containing detected face rectangles
     */
    public MatOfRect detectFaces(Mat gray, double scaleFactor, int minNeighbors) {
        // Handle gray empty error
        if (gray.empty() || gray == null) {
            // chore(), Harry: Throw a custom error or built in error here
        }

        MatOfRect faces = new MatOfRect();

        // Detect Faces
        faceDetector.detectMultiScale(
                gray,
                faces,
                scaleFactor,
                minNeighbors,
                0,
                new Size(DEFAULT_MIN_SIZE, DEFAULT_MIN_SIZE),
                new Size());

        return faces;
    }

    /**
     * Detect faces with default parameters. (Overloaded method)
     * 
     * @param gray The grayscale image to process
     * @return MatOfRect containing detected face rectangles
     */
    public MatOfRect detectFaces(Mat gray) {
        return detectFaces(gray, DEFAULT_SCALE_FACTOR, DEFAULT_MIN_NEIGHBORS);
    }
}