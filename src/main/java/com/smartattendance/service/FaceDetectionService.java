package com.smartattendance.service;

import org.opencv.objdetect.CascadeClassifier;

import com.smartattendance.service.recognition.RecognitionResult;

import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import org.opencv.core.Mat;

public class FaceDetectionService {
    private final CascadeClassifier faceDetector;

    // Default detection parameters
    private static final double DEFAULT_SCALE_FACTOR = 1.1;
    private static final int DEFAULT_MIN_NEIGHBORS = 5;
    private static final int DEFAULT_MIN_SIZE = 30;

    // Color parameters for face detection
    private static final Scalar GREEN_RECT_COLOR = new Scalar(0, 255, 0); // Green
    private static final Scalar YELLOW_RECT_COLOR = new Scalar(0, 255, 255); // Yellow
    private static final Scalar RED_RECT_COLOR = new Scalar(0, 0, 255); // Red
    private static final int DEFAULT_RECT_THICKNESS = 2;

    // Confidence thresholds
    private static final double HIGH_CONFIDENCE_THRESHOLD = 70.0;
    private static final double LOW_CONFIDENCE_THRESHOLD = 30.0;

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
        color = faceCount == 1 ? GREEN_RECT_COLOR : RED_RECT_COLOR;

        // Draw all rectangles with color
        for (Rect rect : facesArray) {
            Imgproc.rectangle(frame, rect.tl(), rect.br(), color, DEFAULT_RECT_THICKNESS);
        }

        return faceCount;
    }

    public int drawFaceRectanglesWithLabels(Mat frame, MatOfRect faces, List<RecognitionResult> results) {
        if (frame == null || frame.empty() || faces == null) {
            return 0;
        }

        Rect[] facesArray = faces.toArray();
        int faceCount = facesArray.length;

        for (int i = 0; i < facesArray.length; i++) {
            Rect rect = facesArray[i];

            // Default values
            String label = "Unkown";
            Scalar color = RED_RECT_COLOR;

            if (results != null && i < results.size()) {
                RecognitionResult result = results.get(i);

                if (result != null && result.isMatch()) {
                    double confidence = result.getConfidenceScore();

                    // Determine color based on confidence
                    if (confidence >= HIGH_CONFIDENCE_THRESHOLD) {
                        color = GREEN_RECT_COLOR;
                    } else if (confidence >= LOW_CONFIDENCE_THRESHOLD) {
                        color = YELLOW_RECT_COLOR;
                    } else {
                        color = RED_RECT_COLOR;
                    }

                    // Set label based on confidence threshold
                    if (confidence >= LOW_CONFIDENCE_THRESHOLD) {
                        label = result.getMatchedStudent().getName();
                    } else {
                        label = "Unknown";
                    }
                }
            }

            // Draw rectangle
            Imgproc.rectangle(frame, rect.tl(), rect.br(), color, DEFAULT_RECT_THICKNESS);

            // Draw label with background

            // Draw background for text
            int baseline[] = { 0 };
            Size textSize = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_SIMPLEX,
                    0.6, 2, baseline);

            Imgproc.rectangle(frame,
                    new org.opencv.core.Point(rect.x, rect.y - textSize.height - 10),
                    new org.opencv.core.Point(rect.x + textSize.width, rect.y),
                    color, -1); // Filled rectangle

            // Draw text
            Imgproc.putText(frame, label,
                    new org.opencv.core.Point(rect.x, rect.y - 5),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(255, 255, 255), 2);

        }

        return faceCount;
    }
}