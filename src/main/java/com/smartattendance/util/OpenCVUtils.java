package com.smartattendance.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Utilities used for opencv and java fx integration
 * 
 */
public final class OpenCVUtils {

	private static final Scalar DEFAULT_RECT_COLOR = new Scalar(0, 255, 0); // Green
	private static final Scalar ERROR_RECT_COLOR = new Scalar(0, 0, 255); // Red
	private static final int DEFAULT_RECT_THICKNESS = 2;

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

	/**
	 * Draw rectangles with color coding based on face count.
	 * The red color shows error if no face, green if valid
	 * 
	 * @param frame The frame to draw on
	 * @param faces The detected faces
	 * @return The number of faces detected
	 */
	public static int drawFaceRectangles(Mat frame, MatOfRect faces) {
		if (frame == null || frame.empty() || faces == null) {
			return 0;
		}

		Rect[] facesArray = faces.toArray();
		int faceCount = facesArray.length;

		// Validate faces by color. face count > 1 -> Green. If not, red
		Scalar color;
		color = faceCount == 1 ? DEFAULT_RECT_COLOR : ERROR_RECT_COLOR;

		// Draw all rectangles with color
		for (Rect rect : facesArray) {
			Imgproc.rectangle(frame, rect.tl(), rect.br(), color, DEFAULT_RECT_THICKNESS);
		}

		return faceCount;
	}
}