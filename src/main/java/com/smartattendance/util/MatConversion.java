package com.smartattendance.util;

import java.nio.ByteBuffer;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MatConversion {
  public static byte[] matHistogramToBytes(Mat histogram) {
    if (histogram == null || histogram.empty()) {
      return null;
    }

    try {
      int rows = histogram.rows();
      int cols = histogram.cols();
      int totalElements = rows * cols;

      // Convert CV_32F to CV_64F first to avoid Mat.get() compatibility issues
      Mat doubleMat = new Mat();
      histogram.convertTo(doubleMat, CvType.CV_64F);

      // Create byte buffer for float data (4 bytes per float)
      ByteBuffer buffer = ByteBuffer.allocate(totalElements * 4);

      // Read data row by row
      double[] rowData = new double[cols];
      for (int i = 0; i < rows; i++) {
        doubleMat.get(i, 0, rowData);
        for (int j = 0; j < cols; j++) {
          buffer.putFloat((float) rowData[j]);
        }
      }

      doubleMat.release();
      return buffer.array();

    } catch (Exception e) {
      System.err.println("Error converting histogram to bytes: " + e.getMessage());
      return null;
    }
  }

  public static Mat bytesToMatHistogram(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return new Mat();
    }

    try {
      int numElements = bytes.length / 4;

      if (numElements != 256) {
        System.err.println("Warning: Expected 256 histogram bins, got " + numElements);
      }

      Mat histogram = new Mat(256, 1, CvType.CV_32F);

      ByteBuffer buffer = ByteBuffer.wrap(bytes);
      float[] data = new float[256];

      for (int i = 0; i < Math.min(256, numElements); i++) {
        data[i] = buffer.getFloat();
      }

      histogram.put(0, 0, data);
      return histogram;

    } catch (Exception e) {
      System.err.println("Error converting bytes to histogram: " + e.getMessage());
      return new Mat();
    }
  }

  public static byte[] matEmbeddingToBytes(Mat embedding) {
    if (embedding == null || embedding.empty()) {
      return null;
    }

    try {
      int rows = embedding.rows();
      int cols = embedding.cols();
      int totalElements = rows * cols;

      ByteBuffer buffer = ByteBuffer.allocate(totalElements * 8);

      double[] data = new double[totalElements];
      embedding.get(0, 0, data);

      for (double value : data) {
        buffer.putDouble(value);
      }

      return buffer.array();

    } catch (Exception e) {
      System.err.println("Error converting embedding to bytes: " + e.getMessage());
      return null;
    }
  }

  public static Mat bytesToMatEmbedding(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return new Mat();
    }

    try {
      int numElements = bytes.length / 8;

      Mat embedding = new Mat(numElements, 1, CvType.CV_64F);

      ByteBuffer buffer = ByteBuffer.wrap(bytes);
      double[] data = new double[numElements];

      for (int i = 0; i < numElements; i++) {
        data[i] = buffer.getDouble();
      }

      embedding.put(0, 0, data);
      return embedding;

    } catch (Exception e) {
      System.err.println("Error converting bytes to embedding: " + e.getMessage());
      return new Mat();
    }
  }

}
