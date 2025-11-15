package com.smartattendance.service.recognition;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.smartattendance.model.entity.Student;
import com.smartattendance.model.entity.FaceData;
import com.smartattendance.service.FaceProcessingService;
import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * Histogram Recognizer
 * Implements face recognition using histogram comparison
 * Computes and compares grayscale histograms of face images
 * Uses correlation-based matching for face identification
 * 
 * @author Min Thet Khine
 */

public class HistogramRecognizer extends Recognizer {
  private final FaceProcessingService faceProcessingService;
  private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

  // Image Dimensions
  private static final int DEFAULT_FACE_WIDTH = 100;
  private static final int DEFAULT_FACE_HEIGHT = 100;

  public HistogramRecognizer(FaceProcessingService faceProcessingService) {
    super();
    this.faceProcessingService = faceProcessingService;
  }

  public HistogramRecognizer(FaceProcessingService faceProcessingService, double confidenceThreshold) {
    super(confidenceThreshold);
    this.faceProcessingService = faceProcessingService;
  }

  // ----- Recoginzer classes -----
  /**
   * Train the recognizer by computing average histograms for each student
   * Processes all enrolled students and generates their face histograms
   * 
   * @param students List of students to train the recognizer with
   */
  @Override
  public void train(List<Student> students) {
    for (Student student : students) {
      FaceData faceData = student.getFaceData();
      if (faceData != null && !faceData.getImages().isEmpty()) {
        try {
          Mat avgHistogram = computeAverageHistogram(faceData.getImages());
          if (avgHistogram.empty()) {
            appLogger.error("Failed to compute average histogram for student " + student.getName());
            continue;
          }

          faceData.setHistogram(avgHistogram);

          appLogger.info("Got average Hist for " + student.getName());
        } catch (Exception e) {
          appLogger.error("Error processing student " + student.getName(), e);
          continue;
        }
      } else {
        appLogger.warn("Student " + student.getName() + " has no face data");
        continue;
      }
    }
  }

  /**
   * Recognize a face by comparing its histogram with stored student histograms
   * Uses correlation comparison to find the best match among enrolled students
   * 
   * @param faceImage        The face image to recognize
   * @param enrolledStudents List of students to compare against
   * @return RecognitionResult containing the matched student and confidence score
   */
  @Override
  public RecognitionResult recognize(Mat faceImage, List<Student> enrolledStudents) {
    if (faceImage.empty() || faceImage == null) {
      appLogger.error("Input face image is empty or null");
      return new RecognitionResult();
    }

    if (enrolledStudents.isEmpty() || enrolledStudents == null) {
      appLogger.error("No enrolled students to compare against");
      return new RecognitionResult();
    }
    try {
      // Preprocess the input face image
      Rect fullRect = new Rect(0, 0, faceImage.cols(), faceImage.rows());
      Mat preprocessedFace = faceProcessingService.preprocessFace(faceImage, fullRect, DEFAULT_FACE_WIDTH,
          DEFAULT_FACE_HEIGHT);

      // Compute histogram for the preprocessed face
      Mat inputHistogram = computeHistogram(preprocessedFace);
      preprocessedFace.release(); // Release memory (Clean up)

      double bestScore = -1.0;
      Student bestMatch = null;

      // Compare with each student's stored histogram
      for (Student student : enrolledStudents) {
        FaceData faceData = student.getFaceData();
        if (faceData == null || faceData.getHistogram() == null) {
          continue;
        }

        try {
          double score = Imgproc.compareHist(
              inputHistogram,
              faceData.getHistogram(), // Get from FaceData!
              Imgproc.CV_COMP_CORREL);

          if (score > bestScore) {
            bestScore = score;
            bestMatch = student;
          }
        } catch (Exception e) {
          appLogger.error("Error comparing histograms for student " + student.getName(), e);
        }
      }

      inputHistogram.release(); // Release memory

      // Return result if meets threshold
      if (bestMatch != null) {
        // Convert correlation score to percentage (correlation is -1 to 1)
        double confidence = (bestScore + 1.0) * 50.0;

        appLogger.info("Best match: " + bestMatch.getName() +
            " with confidence: " + String.format("%.1f%%", confidence));

        return new RecognitionResult(bestMatch, confidence);
      }

      return new RecognitionResult();
    } catch (Exception e) {
      appLogger.error("Error during recognition", e);
      return new RecognitionResult();
    }
  }

  // Recognize multiple faces from a list of face images
  public List<RecognitionResult> recognizeBatch(List<Mat> faceImages, List<Student> enrolledStudents) {
    List<RecognitionResult> results = new java.util.ArrayList<>();

    for (Mat faceImage : faceImages) {
      RecognitionResult result = recognize(faceImage, enrolledStudents);
      results.add(result);
    }

    return results;
  }

  // ----- Histogram Computation -----
  /**
   * Recognize multiple faces from a list of face images
   * Processes each face image individually and returns a list of results
   * 
   * @param faceImages       List of face images to recognize
   * @param enrolledStudents List of students to compare against
   * @return List of recognition results for each face
   */
  public Mat computeHistogram(Mat image) {
    if (image.empty()) {
      appLogger.error("Cannot compute histogram for empty image");
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
      appLogger.error("Error computing histogram: " + e.getMessage());
      if (!hist.empty()) {
        hist.release();
      }
      return new Mat();
    }

  }

  /**
   * Compute normalized histogram for a grayscale image
   * Calculates the distribution of pixel intensities (0-255)
   * 
   * @param image The grayscale image to compute histogram for
   * @return Normalized histogram as Mat, or empty Mat on failure
   */
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
        Mat preprocessedFace = faceProcessingService.preprocessFace(rawImage, fullRect, DEFAULT_FACE_WIDTH,
            DEFAULT_FACE_HEIGHT);

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
        appLogger.info("No valid images to compute average histogram");
        sumHistogram.release();
        return new Mat();
      }

      // Compute average
      sumHistogram.convertTo(sumHistogram, sumHistogram.type(), 1.0 / validCount);
      Core.normalize(sumHistogram, sumHistogram, 0, 1, Core.NORM_MINMAX);

      appLogger.info("Computed average histogram from " + validCount + " images");
      return sumHistogram;
    } catch (Exception e) {
      appLogger.error("Error computing average histogram: " + e.getMessage());
      return new Mat();
    }
  }

}
