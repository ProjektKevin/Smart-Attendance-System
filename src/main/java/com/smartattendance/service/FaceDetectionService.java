package com.smartattendance.service;

import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Mat;

public class FaceDetectionService {
    private final CascadeClassifier faceDetector;

    // Default detection parameters
    private static final double DEFAULT_SCALE_FACTOR = 1.1;
    private static final int DEFAULT_MIN_NEIGHBORS = 3;
    private static final int DEFAULT_MIN_SIZE = 30;

    // Color parameters for face detection
    private static final Scalar DEFAULT_RECT_COLOR = new Scalar(0, 255, 0); // Green
    private static final Scalar ERROR_RECT_COLOR = new Scalar(0, 0, 255); // Red
    private static final int DEFAULT_RECT_THICKNESS = 2;

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

    /**
     * Draw rectangles with color coding based on face count.
     * The red color shows error if no face, green if valid
     * 
     * @param frame The frame to draw on
     * @param faces The detected faces
     * @return The number of faces detected
     */
    public int drawFaceRectangles(Mat frame, MatOfRect faces) {
        if (frame == null || frame.empty() || faces == null) {
            return 0;
        }

        Rect[] facesArray = faces.toArray();
        int faceCount = facesArray.length;

        // Validate faces by color. face count > 1 -> Green. If not, red
        Scalar color;
        color = faceCount == 1 ? DEFAULT_RECT_COLOR : ERROR_RECT_COLOR;

        // Draw all rectangles with color
        for (Rect rect : facesArray) {
            Imgproc.rectangle(frame, rect.tl(), rect.br(), color, DEFAULT_RECT_THICKNESS);
        }

        return faceCount;
    }
}