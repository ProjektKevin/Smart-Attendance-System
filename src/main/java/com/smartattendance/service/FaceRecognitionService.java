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
import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * Face Recognition Service
 * Main service for managing face recognition operations
 * Handles face detection, recognition, student enrollment, and algorithm
 * switching
 * Supports multiple recognition algorithms (Histogram and OpenFace)
 * 
 * @author Min Thet Khine
 */

public class FaceRecognitionService {
  private final FaceDetectionService faceDetectionService;
  private final FaceProcessingService faceProcessingService;
  private final StudentRepository studentRepository;
  private Recognizer recognizer;
  private List<Student> enrolledStudents;

  // Logger
  private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

  // ----- Constructor -----
  /**
   * Constructor
   * Initializes the face recognition service with required dependencies
   * 
   * @param faceDetectionService Service for detecting faces in images
   */
  public FaceRecognitionService(FaceDetectionService faceDetectionService) {
    this.faceDetectionService = faceDetectionService;
    this.faceProcessingService = new FaceProcessingService(faceDetectionService);
    this.studentRepository = new StudentRepository();
    this.enrolledStudents = new ArrayList<>();

    appLogger.info("FaceRecognitionService initialized with HistogramRecognizer");
  }

  // ----- Face Recognition Methods -----
  /**
   * Recognize multiple faces from a list of face ROIs
   * Processes each face ROI and returns recognition results
   * 
   * @param faceROIs List of face regions of interest to recognize
   * @return List of recognition results for each face
   */
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

  /**
   * Recognize a single face from its ROI
   * 
   * @param faceROI The face region of interest to recognize
   * @return RecognitionResult containing the matched student and confidence
   */
  public RecognitionResult recognizeFace(Mat faceROI) {
    if (faceROI == null || faceROI.empty()) {
      appLogger.error("Cannot recognize empty face ROI");
      return new RecognitionResult();
    }

    if (recognizer == null) {
      appLogger.error("ERROR: Recognizer not initialized! Call switchAlgorithm() first.");
      return new RecognitionResult();
    }

    return recognizer.recognize(faceROI, enrolledStudents);
  }

  /**
   * Extract face ROIs from a frame using detected face rectangles
   * Validates bounds and clones the face regions
   * 
   * @param frame     The full frame/image containing faces
   * @param faceRects Array of rectangles indicating face locations
   * @return List of extracted face ROIs
   */
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
          appLogger.warn("Face rectangle out of bounds, skipping");
          continue;
        }

        // Extract face region
        faceROI = new Mat(frame, faceRect);
        faceROIs.add(faceROI.clone()); // Clone to avoid reference issues
      } catch (Exception e) {
        appLogger.error("Error extracting face ROI", e);
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
  /**
   * Load enrolled students for a specific session/course
   * Fetches students from repository and updates internal list
   * 
   * @param sessionId The session ID to load students for
   * @return Number of students loaded
   * @throws SQLException If database error occurs
   */
  public int loadEnrolledStudentsBySessionId(Integer sessionId) throws SQLException {
    appLogger.info("Loading enrolled students in sessionId: " + sessionId);

    // Fetch students from repository
    List<Student> students = studentRepository.fetchEnrolledStudentsByCourse(sessionId);

    // Update internal list
    if (students == null) {
      this.enrolledStudents = new ArrayList<>();
    } else {
      this.enrolledStudents = students;
    }

    appLogger.info("Loaded " + students.size() + " students from sessionId " + sessionId);
    return students.size();
  }

  /**
   * Switch between recognition algorithms
   * Supports OPENFACE and HISTOGRAM algorithms
   * Falls back to HISTOGRAM if OPENFACE is not available
   * 
   * @param algorithmName Name of the algorithm to use ("OPENFACE" or "HISTOGRAM")
   */
  public void switchAlgorithm(String algorithmName) {
    if (algorithmName == null) {
      appLogger.warn("Algorithm name is null, defaulting to HISTOGRAM");
      algorithmName = "HISTOGRAM";
    }

    appLogger.info("Switching recognition algorithm to: " + algorithmName);

    if (algorithmName.equalsIgnoreCase("OPENFACE")) {
      if (ApplicationContext.getOpenFaceRecognizer() == null) {
        appLogger.error("OpenFace not available. Falling back to HISTOGRAM");
        this.recognizer = ApplicationContext.getHistogramRecognizer();
        algorithmName = "HISTOGRAM";
        Config.set("recognition.algorithm", algorithmName);
        return;
      }

      this.recognizer = ApplicationContext.getOpenFaceRecognizer();
      appLogger.info("Using OpenFaceRecognizer (DNN-based)");
    } else {
      this.recognizer = ApplicationContext.getHistogramRecognizer();
      appLogger.info("Using HistogramRecognizer");
      algorithmName = "HISTOGRAM"; // Name Normalization
    }

    Config.set("recognition.algorithm", algorithmName);
  }

  // ----- Debugging Methods -----
  /**
   * Get the name of the currently active recognition algorithm
   * Useful for debugging and logging
   * 
   * @return Algorithm name ("OPENFACE", "HISTOGRAM", "UNKNOWN", or "NONE")
   */
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
