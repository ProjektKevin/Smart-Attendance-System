package com.smartattendance.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Utilities used for opencv and java fx integration
 * 
 */
public final class OpenCVUtils {

	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 *
	 * @param frame
	 *              the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	public static Image mat2Image(Mat frame) {
		try {
			return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
		} catch (Exception e) {
			System.err.println("Cannot convert the Mat obejct: " + e);
			return null;
		}
	}

	/**
	 * Generic method for putting element running on a non-JavaFX thread on the
	 * JavaFX thread, to properly update the UI
	 * 
	 * @param property
	 *                 a {@link ObjectProperty}
	 * @param value
	 *                 the value to set for the given {@link ObjectProperty}
	 */
	public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
		Platform.runLater(() -> {
			property.set(value);
		});
	}

	/**
	 * Support for the {@link mat2image()} method
	 * 
	 * @param original
	 *                 the {@link Mat} object in BGR or grayscale
	 * @return the corresponding {@link BufferedImage}
	 */
	private static BufferedImage matToBufferedImage(Mat original) {
		// init
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);

		if (original.channels() > 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

		return image;
	}

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

			// Handle both CV_32F and CV_64F types
			int matType = embedding.type();
			if (matType == CvType.CV_32F) {
				// Extract as float, convert to double
				float[] floatData = new float[totalElements];
				embedding.get(0, 0, floatData);
				for (float value : floatData) {
					buffer.putDouble((double) value);
				}
			} else {
				// Extract as double directly (CV_64F or other types)
				double[] data = new double[totalElements];
				embedding.get(0, 0, data);
				for (double value : data) {
					buffer.putDouble(value);
				}
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

	/**
	 * Convert OpenCV Mat (128-dimensional embedding) to double array
	 * Handles both CV_32F (float) and CV_64F (double) Mat types
	 */
	public static double[] matToDoubleArray(Mat mat) {
		if (mat == null || mat.empty()) {
			throw new IllegalArgumentException("Mat cannot be null or empty");
		}

		int totalElements = mat.rows() * mat.cols();
		if (totalElements != 128) {
			throw new IllegalArgumentException(
					"Mat must contain exactly 128 elements. Got: " + totalElements);
		}

		Mat flatMat = mat.reshape(0, 1); // flatten to 1x128
		double[] data = new double[128];

		// Check Mat type and handle accordingly
		int matType = flatMat.type();

		if (matType == CvType.CV_32F) {
			// If Mat is float (CV_32F = 5), extract as float first, then convert to double
			float[] floatData = new float[128];
			flatMat.get(0, 0, floatData);
			for (int i = 0; i < 128; i++) {
				data[i] = (double) floatData[i];
			}
		} else if (matType == CvType.CV_64F) {
			// If Mat is double (CV_64F = 6), extract directly
			flatMat.get(0, 0, data);
		} else {
			// For other types, convert to CV_64F first
			Mat doubleMat = new Mat();
			flatMat.convertTo(doubleMat, CvType.CV_64F);
			doubleMat.get(0, 0, data);
			doubleMat.release();
		}

		return data;
	}

	/**
	 * Convert double array to PostgreSQL pgvector string "[v1,v2,...,v128]"
	 */
	public static String doubleArrayToPostgresVector(double[] doubleArray) {
		if (doubleArray == null || doubleArray.length != 128) {
			throw new IllegalArgumentException(
					"Double array must contain exactly 128 elements. Got: " +
							(doubleArray == null ? "null" : doubleArray.length));
		}

		StringBuilder vector = new StringBuilder("[");
		for (int i = 0; i < doubleArray.length; i++) {
			vector.append(String.format("%.15f", doubleArray[i])); // high precision
			if (i < doubleArray.length - 1) {
				vector.append(",");
			}
		}
		vector.append("]");
		return vector.toString();
	}

	/**
	 * Convert OpenCV Mat to PostgreSQL pgvector string in one step
	 */
	public static String matToPostgresVector(Mat mat) {
		double[] data = matToDoubleArray(mat);
		return doubleArrayToPostgresVector(data);
	}

	/**
	 * Convert PostgreSQL pgvector string "[v1,v2,...,v128]" to double array
	 */
	public static double[] postgresVectorToDoubleArray(String vectorString) {
		if (vectorString == null || vectorString.isEmpty()) {
			throw new IllegalArgumentException("Vector string cannot be null or empty");
		}

		vectorString = vectorString.replaceAll("[\\[\\]]", ""); // remove brackets
		String[] parts = vectorString.split(",");
		if (parts.length != 128) {
			throw new IllegalArgumentException("Expected 128 elements, got: " + parts.length);
		}

		double[] data = new double[128];
		for (int i = 0; i < 128; i++) {
			data[i] = Double.parseDouble(parts[i].trim());
		}
		return data;
	}

	/**
	 * Convert PostgreSQL pgvector string "[v1,v2,...,v128]" back to OpenCV Mat
	 */
	public static Mat postgresVectorToMat(String vectorString) {
		double[] data = postgresVectorToDoubleArray(vectorString);
		Mat mat = new Mat(1, 128, CvType.CV_64F); // store as double
		mat.put(0, 0, data);
		return mat;
	}
}