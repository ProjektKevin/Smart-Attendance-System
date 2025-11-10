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

import com.smartattendance.model.entity.AuthSession;
import com.smartattendance.model.entity.FaceData;
import com.smartattendance.model.entity.Image;
import com.smartattendance.model.entity.Student;
import com.smartattendance.repository.ImageRepository;
import com.smartattendance.service.recognition.HistogramRecognizer;
import com.smartattendance.util.OpenCVUtils;
import com.smartattendance.util.security.log.ApplicationLogger;
import com.smartattendance.util.security.log.AttendanceLogger;

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
    private final HistogramRecognizer histogramRecognizer;

    /**
     * Initialize ImageService with repository, processing service, and recognizer
     * All are initialized internally
     */
    public ImageService(FaceProcessingService faceProcessingService) {
        this.imageRepository = new ImageRepository();
        this.histogramRecognizer = new HistogramRecognizer(faceProcessingService);
    }

    /**
     * Save image to disk and insert metadata to database
     *
     * @param studentId the student ID
     * @param frame     the OpenCV Mat frame from camera
     * @return true if save was successful, false otherwise
     */
    public boolean captureAndSaveImage(int studentId, Mat frame) {
        ApplicationLogger.getInstance().info("Running Capture and Save Image Function for Student: " + studentId);

        if (frame == null || frame.empty()) {
            System.err.println("Frame is null or empty");
            return false;
        }

        try {
            // Save image to disk with organized directory structure
            ApplicationLogger.getInstance().info("Saving Image to Local Disk");
            String imagePath = saveImageToDisk(studentId, frame);
            ApplicationLogger.getInstance().info("Image saved to: " + imagePath);

            // Insert metadata to student_image table
            ApplicationLogger.getInstance().info("Inserting Metadata to the Database");
            int imageId = imageRepository.insertStudentImage(studentId, imagePath);

            if (imageId > 0) {
                ApplicationLogger.getInstance().info("Image Recorded with ID " + imageId);
                ApplicationLogger.getInstance().info("Image Saved and Recorded: " + imagePath);
                return true;
            } else {
                ApplicationLogger.getInstance().error("Failed to Insert Image Metadata to Database");
                return false;
            }

        } catch (Exception e) {
            ApplicationLogger.getInstance()
                    .error("Error Capturing Images", e);
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
    public boolean trainAndPersistEnrollment(AuthSession session) {
        // Get student ID from current logged-in user
        Integer studentId = session.getCurrentUser().getId();
        String studentName = session.getCurrentUser().getUserName();
        try {
            AttendanceLogger.getInstance()
                    .info("Starting Enrollment Training for Student: " + studentName + " (ID: " + studentId + ")");

            // Load all images from disk
            List<Mat> enrollmentImages = loadImagesFromDisk(studentId);

            if (enrollmentImages.isEmpty()) {
                AttendanceLogger.getInstance().error("No Images Found to Train for Student " + studentId);
                return false;
            }

            AttendanceLogger.getInstance().info("Loaded " + enrollmentImages.size() + " Images");

            // Create Student and FaceData objects for training
            Student student = new Student(studentId, studentName, "");
            FaceData faceData = new FaceData();

            // Add all images to FaceData
            for (Mat image : enrollmentImages) {
                faceData.addImage(image);
            }

            student.setFaceData(faceData);

            // Train using HistogramRecognizer
            AttendanceLogger.getInstance().info("Computing Average Histogram");
            List<Student> studentList = new ArrayList<>();
            studentList.add(student);

            histogramRecognizer.train(studentList);

            // Get the trained average histogram
            Mat averageHistogram = student.getFaceData().getHistogram();

            if (averageHistogram == null || averageHistogram.empty()) {
                AttendanceLogger.getInstance()
                        .error("Failed to compute average histogram for student " + studentId);
                // Cleanup on failure
                cleanupCapturedImages(studentId);
                return false;
            }

            AttendanceLogger.getInstance().info("Average histogram computed successfully");

            // Persist to database
            AttendanceLogger.getInstance().info("Persisting average histogram to database");
            byte[] histogramBytes = OpenCVUtils.matHistogramToBytes(averageHistogram);

            boolean persistSuccess = imageRepository.insertFaceData(studentId, histogramBytes);

            if (!persistSuccess) {
                AttendanceLogger.getInstance().error("Failed to persist histogram to database");
                // Cleanup on failure
                cleanupCapturedImages(studentId);
                return false;
            }

            AttendanceLogger.getInstance().info("Histogram Persisted to Database");

            // Cleanup captured images
            ApplicationLogger.getInstance().info("Cleaning Up Captured Images");
            cleanupCapturedImages(studentId);

            AttendanceLogger.getInstance().info("Completed Enrollment for " + studentId);

            // Cleanup in-memory images
            for (Mat image : enrollmentImages) {
                image.release();
            }

            return true;

        } catch (Exception e) {
            AttendanceLogger.getInstance().error("Error Training Enrollment Data for : " + studentName, e);
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
    private List<Mat> loadImagesFromDisk(Integer studentId) {
        List<Mat> images = new ArrayList<>();

        String studentDir = CAPTURE_DIR + File.separator + studentId;
        File dir = new File(studentDir);

        if (!dir.exists() || !dir.isDirectory()) {
            ApplicationLogger.getInstance().error("Capture directory not found: " + studentDir);
            return images;
        }

        // Get all jpg/png files
        File[] imageFiles = dir.listFiles((dirfile, name) -> name.toLowerCase().endsWith(".jpg") ||
                name.toLowerCase().endsWith(".png") ||
                name.toLowerCase().endsWith(".jpeg"));

        if (imageFiles == null || imageFiles.length == 0) {
            ApplicationLogger.getInstance().error("No image files found in: " + studentDir);
            return images;
        }

        for (File imageFile : imageFiles) {
            try {
                Mat image = Imgcodecs.imread(imageFile.getAbsolutePath());
                if (image != null && !image.empty()) {
                    images.add(image);
                } else {
                    ApplicationLogger.getInstance().error("Failed to load image: " + imageFile.getName());
                }
            } catch (Exception e) {
                ApplicationLogger.getInstance()
                        .error("Error loading image " + imageFile.getName() + ": " + e.getMessage(), e);
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
    private void cleanupCapturedImages(Integer studentId) {
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

                ApplicationLogger.getInstance().info("Cleaned up captured images for student " + studentId);
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
        ApplicationLogger.getInstance().info("Student directory: " + studentDir);

        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            ApplicationLogger.getInstance().info("Directory created: " + created);
        } else {
            ApplicationLogger.getInstance().info("Directory already exists");
        }

        // Create filename with timestamp
        String filename = studentDir + File.separator + "face_" + System.currentTimeMillis() + ".jpg";
        ApplicationLogger.getInstance().info("Saving image to: " + filename);

        // Save the image
        boolean success = Imgcodecs.imwrite(filename, frame);
        ApplicationLogger.getInstance().info("Imgcodecs.imwrite result: " + success);

        if (!success) {
            throw new RuntimeException("Failed to write image to disk: " + filename);
        }

        ApplicationLogger.getInstance().info("Image successfully saved");
        return filename;
    }

    /**
     * Load histogram from byte array (from database)
     * Used for face recognition when comparing against enrolled student histograms
     *
     * @param histogramBytes the histogram as byte array
     * @return Mat histogram object for comparison
     */
    public Mat loadHistogramFromBytes(byte[] histogramBytes) {
        return OpenCVUtils.bytesToMatHistogram(histogramBytes);
    }

    /**
     * Get trained histogram for a student from database
     * Used during face recognition to compare against captured face
     *
     * @param studentId the student ID
     * @return Mat histogram, or empty Mat if not found
     */
    public Mat getStudentHistogram(int studentId) {
        byte[] histogramBytes = imageRepository.getHistogramByStudentId(studentId);
        if (histogramBytes == null) {
            System.err.println("No histogram found for student: " + studentId);
            return new Mat();
        }
        return loadHistogramFromBytes(histogramBytes);
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
