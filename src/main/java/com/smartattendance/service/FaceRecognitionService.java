package com.smartattendance.service;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import com.smartattendance.model.entity.Student;
import com.smartattendance.service.recognition.HistogramRecognizer;
import com.smartattendance.service.recognition.RecognitionResult;
import com.smartattendance.service.recognition.Recognizer;

public class FaceRecognitionService {
  private final FaceDetectionService faceDetectionService;
  private final FaceProcessingService faceProcessingService;
  private Recognizer recognizer;
  private List<Student> enrolledStudents;
  private RecognitionAlgorithm currentAlgorithm;

  public enum RecognitionAlgorithm {
    HISTOGRAM,
    DNN 
  }

  // ----- Constructor -----
  public FaceRecognitionService(FaceDetectionService faceDetectionService) {
    this.faceDetectionService = faceDetectionService;
    this.faceProcessingService = new FaceProcessingService(faceDetectionService);
    this.recognizer = new HistogramRecognizer(faceProcessingService, 70.0);
    this.enrolledStudents = new ArrayList<>();

    System.out.println("FaceRecognitionService initialized with HistogramRecognizer");
  }

  // ----- Face Recognition Methods -----
  public List<RecognitionResult> recognizeFaces(List<Mat> faceROIs) {
    List<RecognitionResult> results = new ArrayList<>();

    for (Mat faceROI : faceROIs) {
      RecognitionResult result = recognizeFace(faceROI);
      results.add(result);
    }

    return results;
  }

  public RecognitionResult recognizeFace(Mat faceROI) {
    if (faceROI == null || faceROI.empty()) {
      System.out.println("Cannot recognize empty face ROI");
      return new RecognitionResult();
    }

    return recognizer.recognize(faceROI, enrolledStudents);
  }

  public List<Mat> extractFaceROIs(Mat frame, Rect[] faceRects) {
    List<Mat> faceROIs = new ArrayList<>();

    if (frame == null || frame.empty() || faceRects == null) {
      return faceROIs;
    }

    for (Rect faceRect : faceRects) {
      try {
        // Validate rectangle bounds
        if (faceRect.x < 0 || faceRect.y < 0 ||
            faceRect.x + faceRect.width > frame.cols() ||
            faceRect.y + faceRect.height > frame.rows()) {
          System.out.println("Face rectangle out of bounds, skipping");
          continue;
        }

        // Extract face region
        Mat faceROI = new Mat(frame, faceRect);
        faceROIs.add(faceROI.clone()); // Clone to avoid reference issues

      } catch (Exception e) {
        System.out.println("Error extracting face ROI: " + e.getMessage());
      }
    }

    return faceROIs;
  }

  // ----- Student Session Management ------
  public int loadSessionStudents() {
    return 0; // Placeholder for actual implementation
  }
}
