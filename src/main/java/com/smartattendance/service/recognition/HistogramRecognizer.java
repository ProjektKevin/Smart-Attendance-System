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

public class HistogramRecognizer extends Recognizer {
  private final FaceProcessingService faceProcessingService;

  public HistogramRecognizer(FaceProcessingService faceProcessingService) {
    super();
    this.faceProcessingService = faceProcessingService;
  }

  public HistogramRecognizer(FaceProcessingService faceProcessingService, double confidenceThreshold) {
    super(confidenceThreshold);
    this.faceProcessingService = faceProcessingService;
  }

  @Override
  public void train(List<Student> students) {
    for (Student student : students) {
      FaceData faceData = student.getFaceData();
      if (faceData != null && !faceData.getImages().isEmpty()) {
        try {
          Mat avgHistogram = faceProcessingService.computeAverageHistogram(faceData.getImages());
          if (avgHistogram.empty()) {
            System.out.println("Failed to compute average histogram for student " + student.getName());
            continue;
          }

          faceData.setHistogram(avgHistogram);

          System.out.println("Got average Hist for " + student.getName());
        } catch (Exception e) {
          System.out.println("Error processing student " + student.getName() + ": " + e.getMessage());
          continue;
        }
      } else {
        System.out.println("Student " + student.getName() + " has no face data");
        continue;
      }
    }
  }

  @Override
  public RecognitionResult recognize(Mat faceImage, List<Student> enrolledStudents) {
    if (faceImage.empty() || faceImage == null) {
      System.out.println("Input face image is empty or null");
      return new RecognitionResult();
    }

    if (enrolledStudents.isEmpty() || enrolledStudents == null) {
      System.out.println("No enrolled students to compare against");
      return new RecognitionResult();
    }
    try {
      // Preprocess the input face image
      Rect fullRect = new Rect(0, 0, faceImage.cols(), faceImage.rows());
      Mat preprocessedFace = faceProcessingService.preprocessFace(faceImage, fullRect);

      // Compute histogram for the preprocessed face
      Mat inputHistogram = faceProcessingService.computeHistogram(preprocessedFace);
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
          System.out.println("Error comparing histograms for student " + student.getName() + ": " + e.getMessage());
        }
      }

      inputHistogram.release(); // Release memory

      // Return result if meets threshold
      if (bestMatch != null) {
        // Convert correlation score to percentage (correlation is -1 to 1)
        double confidence = (bestScore + 1.0) * 50.0;

        if (confidence >= getConfidenceThreshold()) {
          System.out.println("Recognized: " + bestMatch.getName() +
              " (confidence: " + String.format("%.2f%%", confidence) + ")");
          return new RecognitionResult(bestMatch, confidence);
        } else {
          System.out.println("Best match below threshold: " + bestMatch.getName() +
              " (confidence: " + String.format("%.2f%%", confidence) + ")");
        }
      }

      return new RecognitionResult();
    } catch (Exception e) {
      System.out.println("Error during recognition: " + e.getMessage());
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


}
