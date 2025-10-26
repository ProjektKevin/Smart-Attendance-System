package com.smartattendance.service;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class FaceProcessingService {
    private final FaceDetectionService faceDetectionService;

    public FaceProcessingService(FaceDetectionService faceDetectionService) {
        this.faceDetectionService = faceDetectionService;
    }

    // ===== Main Function =====
    public Mat preprocessFace(Mat faceImg, Rect faceRect, int targetWidth, int targetHeight) {
        // Step 1: Greyscale the feace
        Mat grayImg = convertToGrayscale(faceImg);
        if (grayImg.empty()) {
            return new Mat();
        }

        // Step 2: Crop the face
        Mat croppedFace = cropDetectedFace(grayImg, faceRect);
        if (croppedFace.empty()) {
            return new Mat();
        }
        
        // Step 3: Resize the face
        Mat resizedFace = resizeImage(croppedFace, targetWidth, targetHeight);
        if (resizedFace.empty()) {
            croppedFace.release();
            return new Mat();
        }
        
        // Step 4: Compute histogram
        Mat histogram = computeHistogram(resizedFace);
        
        // Clean up Memory Leaks
        croppedFace.release();
        resizedFace.release();
        
        return histogram;
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
            return new Mat();       // Return empty Mat or just throw custom Error 
        }
        
        // Ensure the rectangle is within image bounds
        if (faceRect.x < 0 || faceRect.y < 0 || 
            faceRect.x + faceRect.width > grayImg.cols() || 
            faceRect.y + faceRect.height > grayImg.rows()) {
            System.out.println("Face rectangle is out of image bounds");
            return new Mat();       // Return empty Mat or just throw custom Error
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
        
        // Histogram parameters
        MatOfInt histSize = new MatOfInt(256);  
        MatOfFloat ranges = new MatOfFloat(0f, 256f);  
        MatOfInt channels = new MatOfInt(0);  
        
        // Calculate histogram
        Imgproc.calcHist(List.of(image), channels, new Mat(), hist, histSize, ranges);
        
        // Normalize histogram to range [0, 1]
        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);
        
        return hist;
    }

    
    
}
