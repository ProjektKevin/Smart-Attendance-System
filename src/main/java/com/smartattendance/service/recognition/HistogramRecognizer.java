package com.smartattendance.service.recognition;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.smartattendance.model.dto.student.FaceDataDTO;
import com.smartattendance.model.dto.student.StudentDTO;
import com.smartattendance.model.entity.Student;

public class HistogramRecognizer extends Recognizer {
  public HistogramRecognizer() {
    super();
  }

  public HistogramRecognizer(double confidenceThreshold) {
    super(confidenceThreshold);
  }

  @Override
  public void train(List<StudentDTO> students) {
    for (StudentDTO student : students) {
      FaceDataDTO faceData = student.getFaceData();
      if (faceData != null && !faceData.getImages().isEmpty()) {
        Mat avgHistogram = computeAverageHistogram(faceData.getImages());
        faceData.setHistogram(avgHistogram);
      }
    }
  }

  @Override
  public RecognitionResult recognize(Mat faceImage, List<StudentDTO> enrolledStudents) {
    if (faceImage.empty()) {
      return new RecognitionResult();
    }

    // Compute histogram of input face
    Mat inputHistogram = computeHistogram(faceImage);

    double bestScore = -1.0;
    StudentDTO bestMatch = null;

    // Compare with each student's stored histogram
    for (StudentDTO student : enrolledStudents) {
      FaceDataDTO faceData = student.getFaceData();
      if (faceData == null || faceData.getHistogram() == null) {
        continue;
      }

      double score = Imgproc.compareHist(
          inputHistogram,
          faceData.getHistogram(), // Get from FaceData!
          Imgproc.CV_COMP_CORREL);

      // Convert correlation to percentage
      double confidence = (score + 1.0) * 50.0;

      if (confidence > bestScore) {
        bestScore = confidence;
        bestMatch = student;
      }
    }

    // Return result if meets threshold
    if (bestMatch != null && bestScore >= getConfidenceThreshold()) {
      return new RecognitionResult(bestMatch, bestScore);
    }

    return new RecognitionResult();
  }

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

  public Mat computeAverageHistogram(List<Mat> images) {
    if (images.isEmpty()) {
      return new Mat();
    }

    Mat sumHistogram = Mat.zeros(256, 1, CvType.CV_32F); // safer initialization

    for (Mat image : images) {
      Mat hist = computeHistogram(image);
      if (hist.type() != CvType.CV_32F || !hist.size().equals(new Size(1, 256))) {
        hist.convertTo(hist, CvType.CV_32F);
        hist = hist.reshape(1, 256); // ensure column vector
      }
      Core.add(sumHistogram, hist, sumHistogram);
    }

    sumHistogram.convertTo(sumHistogram, sumHistogram.type(), 1.0 / images.size());
    Core.normalize(sumHistogram, sumHistogram, 0, 1, Core.NORM_MINMAX);

    return sumHistogram;
  }

}
