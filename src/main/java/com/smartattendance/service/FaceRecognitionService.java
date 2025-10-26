package com.smartattendance.service;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class FaceRecognitionService {
  private final FaceDetectionService faceDetectionService;

  public FaceRecognitionService(FaceDetectionService faceDetectionService) {
    this.faceDetectionService = faceDetectionService;
  }

  //

  // ----- Compute Histogram for Face Recognition -----
  public static int getBestHistogramMatchIndex(Mat targetHist, List<Mat> histograms) {
    double bestScore = -1.0;
    int bestIndex = -1;

    for (int i = 0; i < histograms.size(); i++) {
      double score = compareHistograms(targetHist, histograms.get(i));
      if (score > bestScore) {
        bestScore = score;
        bestIndex = i;
      }
    }

    System.out.println("Best Score: " + bestScore);
    System.out.println("Best Index: " + bestIndex);
    
    return bestIndex;
  }

  public static double compareHistograms(Mat hist1, Mat hist2) {
    if (hist1.empty() || hist2.empty()) {
      System.out.println("Histograms not found!");
      return -1.0;
    }

    return Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_CORREL);
  }

}
