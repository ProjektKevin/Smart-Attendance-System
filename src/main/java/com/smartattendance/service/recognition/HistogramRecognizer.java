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
  @Override
  public void train(List<Student> students) {
    for (Student student : students) {
      FaceData faceData = student.getFaceData();
      if (faceData != null && !faceData.getImages().isEmpty()) {
        try {
          Mat avgHistogram = computeAverageHistogram(faceData.getImages());
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
          System.out.println("Error comparing histograms for student " + student.getName() + ": " + e.getMessage());
        }
      }

      inputHistogram.release(); // Release memory

      // Return result if meets threshold
      if (bestMatch != null) {
        // Convert correlation score to percentage (correlation is -1 to 1)
        double confidence = (bestScore + 1.0) * 50.0;

        System.out.println("Best match: " + bestMatch.getName());
        return new RecognitionResult(bestMatch, confidence);
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

  // ----- Histogram Computation -----
  public Mat computeHistogram(Mat image) {
    if (image.empty()) {
      System.out.println("Cannot compute histogram for empty image");
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
      System.err.println("Error computing histogram: " + e.getMessage());
      if (!hist.empty()) {
        hist.release();
      }
      return new Mat();
    }

  }

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
        Mat preprocessedFace = faceProcessingService.preprocessFace(rawImage, fullRect, DEFAULT_FACE_WIDTH, DEFAULT_FACE_HEIGHT);

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
        System.out.println("No valid images to compute average histogram");
        sumHistogram.release();
        return new Mat();
      }

      // Compute average
      sumHistogram.convertTo(sumHistogram, sumHistogram.type(), 1.0 / validCount);
      Core.normalize(sumHistogram, sumHistogram, 0, 1, Core.NORM_MINMAX);

      System.out.println("Computed average histogram from " + validCount + " images");
      return sumHistogram;
    } catch (Exception e) {
      System.err.println("Error computing average histogram: " + e.getMessage());
      return new Mat();
    }
  }

}
