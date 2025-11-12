package com.smartattendance.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;
import com.smartattendance.model.entity.Student;
import com.smartattendance.repository.StudentRepository;
import com.smartattendance.service.recognition.HistogramRecognizer;
import com.smartattendance.service.recognition.OpenFaceRecognizer;
import com.smartattendance.service.recognition.RecognitionResult;
import com.smartattendance.service.recognition.Recognizer;

public class FaceRecognitionService {
  private final FaceDetectionService faceDetectionService;
  private final FaceProcessingService faceProcessingService;
  private final StudentRepository studentRepository;
  private Recognizer recognizer;
  private List<Student> enrolledStudents;

  // ----- Constructor -----
  public FaceRecognitionService(FaceDetectionService faceDetectionService) {
    this.faceDetectionService = faceDetectionService;
    this.faceProcessingService = new FaceProcessingService(faceDetectionService);
    this.studentRepository = new StudentRepository();
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

    // Clean up
    for (Mat faceROI : faceROIs) {
      if (faceROI != null && !faceROI.empty()) {
        faceROI.release();
      }
    }

    return results;
  }

  public RecognitionResult recognizeFace(Mat faceROI) {
    if (faceROI == null || faceROI.empty()) {
      System.out.println("Cannot recognize empty face ROI");
      return new RecognitionResult();
    }

    if (recognizer == null) {
      System.err.println("ERROR: Recognizer not initialized! Call switchAlgorithm() first.");
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
      Mat faceROI = null;
      try {
        // Validate rectangle bounds
        if (faceRect.x < 0 || faceRect.y < 0 ||
            faceRect.x + faceRect.width > frame.cols() ||
            faceRect.y + faceRect.height > frame.rows()) {
          System.out.println("Face rectangle out of bounds, skipping");
          continue;
        }

        // Extract face region
        faceROI = new Mat(frame, faceRect);
        faceROIs.add(faceROI.clone()); // Clone to avoid reference issues
      } catch (Exception e) {
        System.out.println("Error extracting face ROI: " + e.getMessage());
      } finally {
        // Release the original Mat (we only need the clone)
        if (faceROI != null) {
          faceROI.release();
        }
      }
    }

    return faceROIs;
  }

  // ----- Student Session Management ------
  public int loadEnrolledStudentsBySessionId(Integer sessionId) throws SQLException {
    System.out.println("Loading enrolled students in sessionId: " + sessionId);

    // Fetch students from repository
    List<Student> students = studentRepository.fetchEnrolledStudentsByCourse(sessionId);

    // Update internal list
    if (students == null) {
      this.enrolledStudents = new ArrayList<>();
    } else {
      this.enrolledStudents = students;
    }

    System.out.println("Loaded " + students.size() + " students from sessionId " + sessionId);
    return students.size();
  }

  public void switchAlgorithm(String algorithmName) {
    if (algorithmName == null) {
      System.out.println("Algorithm name is null, defaulting to HISTOGRAM");
      algorithmName = "HISTOGRAM";
    }

    System.out.println("Switching recognition algorithm to: " + algorithmName);

    if (algorithmName.equalsIgnoreCase("OPENFACE")) {
      if (ApplicationContext.getOpenFaceRecognizer() == null) {
        System.err.println("OpenFace not available. Falling back to HISTOGRAM");
        this.recognizer = ApplicationContext.getHistogramRecognizer();
        algorithmName = "HISTOGRAM";
        Config.set("recognition.algorithm", algorithmName);
        return;
      }

      this.recognizer = ApplicationContext.getOpenFaceRecognizer();
      System.out.println("Using OpenFaceRecognizer (DNN-based)");
    } else {
      this.recognizer = ApplicationContext.getHistogramRecognizer();
      System.out.println("Using HistogramRecognizer");
      algorithmName = "HISTOGRAM"; // Name Normalization
    }

    Config.set("recognition.algorithm", algorithmName);
  }

  // ----- Debugging Methods -----
  // usage: System.out.println("Current algorithm: " +
  // faceRecognitionService.getCurrentAlgorithm());
  public String getCurrentAlgorithm() {
    if (recognizer == null) {
      return "NONE (not initialized)";
    }

    if (recognizer instanceof OpenFaceRecognizer) {
      return "OPENFACE";
    } else if (recognizer instanceof HistogramRecognizer) {
      return "HISTOGRAM";
    } else {
      return "UNKNOWN";
    }
  }
}
