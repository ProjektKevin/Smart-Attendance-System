package com.smartattendance.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.FaceData;
import com.smartattendance.model.entity.Image;
import com.smartattendance.model.entity.Student;
import com.smartattendance.repository.ImageRepository;
import com.smartattendance.service.recognition.HistogramRecognizer;
import com.smartattendance.util.security.LoggerUtil;

/**
 * Service layer for handling student image enrollment and face data persistence
 *
 * Save images to disk + metadata to DB
 * Load all images, train using HistogramRecognizer (compute
 * average histogram)
 * Save average histogram to face_data table in DB
 * Delete captured images folder to free disk space
 * 
 * @author Thiha Swan Htet
 */
public class ImageService {

    private static final String CAPTURE_DIR = "captured_faces";

    private final ImageRepository imageRepository;
    private final FaceProcessingService faceProcessingService;
    private final HistogramRecognizer histogramRecognizer;

    /**
     * Initialize ImageService with repository, processing service, and recognizer
     * All are initialized internally
     */
    public ImageService() {
        this.imageRepository = new ImageRepository();
        this.faceProcessingService = new FaceProcessingService(ApplicationContext.getFaceDetectionService());
        this.histogramRecognizer = new HistogramRecognizer(this.faceProcessingService);
    }

    /**
     * Save image to disk and insert metadata to database
     *
     * @param studentId the student ID
     * @param frame     the OpenCV Mat frame from camera
     * @return true if save was successful, false otherwise
     */
    public boolean captureAndSaveImage(int studentId, Mat frame) {
        if (frame == null || frame.empty()) {
            System.err.println("Cannot capture: frame is empty");
            return false;
        }

        try {
            // Step 1: Save image to disk with organized directory structure
            String imagePath = saveImageToDisk(studentId, frame);

            // Step 2: Insert metadata to student_image table
            int imageId = imageRepository.insertStudentImage(studentId, imagePath);

            if (imageId > 0) {
                LoggerUtil.LOGGER.info("Image saved and recorded: " + imagePath);
                return true;
            } else {
                System.err.println("Failed to insert image metadata to database");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error capturing image: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Train, Persist, and Cleanup
     * Load all captured images → Train to get average histogram → Save to DB →
     * Delete folder
     * Gets student ID from ApplicationContext (current logged-in user)
     * This runs synchronously (caller should handle blocking if needed)
     *
     * @return true if training and persistence successful, false otherwise
     */
    public boolean trainAndPersistEnrollment() {
        try {
            // Get student ID from current logged-in user
            int studentId = ApplicationContext.getCurrentUser().getUser_id();
            String studentName = ApplicationContext.getCurrentUser().getName();

            LoggerUtil.LOGGER.info("Starting enrollment training for student: " + studentName + " (ID: " + studentId + ")");

            // Load all images from disk
            List<Mat> enrollmentImages = loadImagesFromDisk(studentId);

            if (enrollmentImages.isEmpty()) {
                System.err.println("No images found to train for student " + studentId);
                return false;
            }

            LoggerUtil.LOGGER.info("Loaded " + enrollmentImages.size() + " images");

            // Create Student and FaceData objects for training
            Student student = new Student(studentId, studentName, "");
            FaceData faceData = new FaceData();

            // Add all images to FaceData
            for (Mat image : enrollmentImages) {
                faceData.addImage(image);
            }

            student.setFaceData(faceData);

            // Train using HistogramRecognizer
            LoggerUtil.LOGGER.info("Computing average histogram");
            List<Student> studentList = new ArrayList<>();
            studentList.add(student);

            histogramRecognizer.train(studentList);

            // Get the trained average histogram
            Mat averageHistogram = student.getFaceData().getHistogram();

            if (averageHistogram == null || averageHistogram.empty()) {
                System.err.println("Failed to compute average histogram for student " + studentId);
                // Cleanup on failure
                cleanupCapturedImages(studentId);
                return false;
            }

            LoggerUtil.LOGGER.info("Average histogram computed successfully");

            // Persist to database
            LoggerUtil.LOGGER.info("Persisting average histogram to database");
            String histogramData = serializeHistogram(averageHistogram);

            boolean persistSuccess = imageRepository.insertFaceData(0, studentId, histogramData);

            if (!persistSuccess) {
                System.err.println("Failed to persist histogram to database");
                // Cleanup on failure
                cleanupCapturedImages(studentId);
                return false;
            }

            LoggerUtil.LOGGER.info("Histogram persisted to database");

            // Cleanup captured images
            LoggerUtil.LOGGER.info("Cleaning up captured images");
            cleanupCapturedImages(studentId);

            LoggerUtil.LOGGER.info("Completed Enrollment for " + studentId);

            // Cleanup in-memory images
            for (Mat image : enrollmentImages) {
                image.release();
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error during enrollment training: " + e.getMessage());
            e.printStackTrace();
            // Cleanup on error
            cleanupCapturedImages(studentId);
            return false;
        }
    }

    /**
     * Load all captured images for a student from disk
     *
     * @param studentId the student ID
     * @return list of Mat images loaded from disk
     */
    private List<Mat> loadImagesFromDisk(int studentId) {
        List<Mat> images = new ArrayList<>();

        String studentDir = CAPTURE_DIR + File.separator + studentId;
        File dir = new File(studentDir);

        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Capture directory not found: " + studentDir);
            return images;
        }

        // Get all jpg/png files
        File[] imageFiles = dir.listFiles((dirfile, name) -> name.toLowerCase().endsWith(".jpg") ||
                name.toLowerCase().endsWith(".png") ||
                name.toLowerCase().endsWith(".jpeg"));

        if (imageFiles == null || imageFiles.length == 0) {
            System.err.println("No image files found in: " + studentDir);
            return images;
        }

        for (File imageFile : imageFiles) {
            try {
                Mat image = Imgcodecs.imread(imageFile.getAbsolutePath());
                if (image != null && !image.empty()) {
                    images.add(image);
                } else {
                    System.err.println("Failed to load image: " + imageFile.getName());
                }
            } catch (Exception e) {
                System.err.println("Error loading image " + imageFile.getName() + ": " + e.getMessage());
            }
        }

        return images;
    }

    /**
     * Delete captured images folder for a student
     * Runs after successful enrollment training
     *
     * @param studentId the student ID
     */
    private void cleanupCapturedImages(int studentId) {
        String studentDir = CAPTURE_DIR + File.separator + studentId;
        Path path = Paths.get(studentDir);

        try {
            if (Files.exists(path)) {
                // Delete directory and all contents recursively
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .forEach(filePath -> {
                            try {
                                Files.delete(filePath);
                            } catch (Exception e) {
                                System.err.println("Failed to delete: " + filePath);
                            }
                        });

                LoggerUtil.LOGGER.info("Cleaned up captured images for student " + studentId);
            }
        } catch (Exception e) {
            System.err.println("Error cleaning up captured images: " + e.getMessage());
        }
    }

    /**
     * Helper: Save image to organized disk directory
     * Directory structure: captured_faces/student_123/face_timestamp.jpg
     *
     * @param studentId the student ID
     * @param frame     the OpenCV Mat to save
     * @return the file path of saved image
     */
    private String saveImageToDisk(int studentId, Mat frame) {
        // Create student-specific directory
        String studentDir = CAPTURE_DIR + File.separator + studentId;
        File dir = new File(studentDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Create filename with timestamp
        String filename = studentDir + File.separator + "face_" + System.currentTimeMillis() + ".jpg";

        // Save the image
        boolean success = Imgcodecs.imwrite(filename, frame);

        if (!success) {
            throw new RuntimeException("Failed to write image to disk: " + filename);
        }

        return filename;
    }

    /**
     * Convert histogram Mat to serialized string format (CSV)
     * The histogram is a 256x1 matrix (one value for each gray level)
     *
     * @param histogram the histogram Mat
     * @return CSV string representation (e.g., "0.1,0.2,0.3,...")
     */
    private String serializeHistogram(Mat histogram) {
        StringBuilder sb = new StringBuilder();

        // Iterate through each bin in the histogram (256 bins for grayscale)
        for (int i = 0; i < histogram.rows(); i++) {
            double value = histogram.get(i, 0)[0];
            if (i > 0)
                sb.append(",");
            sb.append(String.format("%.4f", value)); // Format to 4 decimal places
        }

        return sb.toString();
    }

    /**
     * Retrieve all images for a specific student from the database
     *
     * @param studentId the student ID
     * @return list of Image objects for the student, or empty list if none found
     */
    public List<Image> getImagesByStudentId(int studentId) {
        return imageRepository.findImagesByStudentId(studentId);
    }

    /**
     * Retrieve a single image by its ID
     *
     * @param imageId the image ID
     * @return Image object if found, null otherwise
     */
    public Image getImageById(int imageId) {
        return imageRepository.findImageById(imageId);
    }

    /**
     * Get the count of images for a student
     *
     * @param studentId the student ID
     * @return number of images enrolled for the student
     */
    public int getImageCountByStudentId(int studentId) {
        return imageRepository.countImagesByStudentId(studentId);
    }
}
