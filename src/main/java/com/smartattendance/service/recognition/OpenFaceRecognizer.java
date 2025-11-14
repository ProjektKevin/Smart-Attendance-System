package com.smartattendance.service.recognition;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import com.smartattendance.model.entity.Student;
import com.smartattendance.model.entity.FaceData;
import com.smartattendance.service.FaceProcessingService;
import com.smartattendance.util.FileLoader;
import com.smartattendance.util.security.log.ApplicationLogger;
import com.smartattendance.util.security.log.AttendanceLogger;

public class OpenFaceRecognizer extends Recognizer {
    private final FaceProcessingService faceProcessingService;
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();
    private final AttendanceLogger attendanceLogger = AttendanceLogger.getInstance();
    private Net faceNet;

    // Model configuration
    private static final String MODEL_RESOURCE_PATH = "/openFaceModels/nn4.small2.v1.t7";

    // OpenFace model parameters
    private static final int INPUT_WIDTH = 96;
    private static final int INPUT_HEIGHT = 96;
    private static final double SCALE_FACTOR = 1.0 / 255.0;
    private static final Scalar MEAN = new Scalar(0, 0, 0);
    private static final boolean SWAP_RB = false;
    private static final int EMBEDDING_SIZE = 128;

    public OpenFaceRecognizer(FaceProcessingService faceProcessingService) {
        super();
        this.faceProcessingService = faceProcessingService;
        loadModel();
    }

    public OpenFaceRecognizer(FaceProcessingService faceProcessingService, double confidenceThreshold) {
        super(confidenceThreshold);
        this.faceProcessingService = faceProcessingService;
        loadModel();
    }

    /**
     * Load the OpenFace DNN model from resources using FileLoader.
     * The model file (nn4.small2.v1.t7) must be present in
     * src/main/resources/openFaceModels/
     */
    private void loadModel() {
        try {
            // Check if resource exists before attempting to load
            if (!FileLoader.exists(MODEL_RESOURCE_PATH)) {
                System.err.println("ERROR: OpenFace model file is missing!");
                System.err.println("Please ensure nn4.small2.v1.t7 is located at: src/main/resources/openFaceModels/");
                return;
            }

            // Load model file to temporary location
            String modelPath = FileLoader.loadToTempFile(MODEL_RESOURCE_PATH);

            // Load the pre-trained OpenFace model
            // OpenFace model outputs 128-dimensional embeddings
            faceNet = Dnn.readNetFromTorch(modelPath);

            if (faceNet.empty()) {
                System.err.println("ERROR: OpenFace model loaded but network is empty!");
                System.err.println("The model file may be corrupted or incompatible.");
                return;
            }

            // Set backend and target for optimal performance
            faceNet.setPreferableBackend(Dnn.DNN_BACKEND_OPENCV);
            faceNet.setPreferableTarget(Dnn.DNN_TARGET_CPU);

            appLogger.info("OpenFace face recognition model initialized");

        } catch (Exception e) {
            System.err.println("ERROR: Failed to load OpenFace model");
            e.printStackTrace();
        }
    }

    /**
     * Check if the model is loaded and ready for use.
     * 
     * @return true if model is loaded, false otherwise
     */
    public boolean isModelLoaded() {
        return faceNet != null && !faceNet.empty();
    }

    // ----- Recoginzer classes -----
    @Override
    public void train(List<Student> students) {
        if (faceNet == null || faceNet.empty()) {
            appLogger.warn("Cannot train: OpenFace model not loaded");
            System.err.println("ERROR: Cannot train recognizer - model not loaded");
            return;
        }

        appLogger.info("Training OpenFace recognizer for " + students.size() + " students");
        appLogger.info("Training OpenFace recognizer...");

        int successCount = 0;
        int failureCount = 0;

        for (Student student : students) {
            FaceData faceData = student.getFaceData();

            if (faceData == null || faceData.getImages().isEmpty()) {
                attendanceLogger.info("Student " + student.getName() + " has no face data - skipping");
                failureCount++;
                continue;
            }

            try {
                List<Mat> embeddings = new ArrayList<>();

                // Compute embeddings for all face images
                for (Mat rawImage : faceData.getImages()) {
                    if (rawImage.empty()) {
                        continue;
                    }

                    // Preprocess the face (convert to grayscale, then to RGB for OpenFace)
                    Rect fullRect = new Rect(0, 0, rawImage.cols(), rawImage.rows());
                    Mat preprocessedFace = faceProcessingService.preprocessFace(
                            rawImage,
                            fullRect,
                            INPUT_WIDTH,
                            INPUT_HEIGHT,
                            false);

                    if (preprocessedFace.empty()) {
                        continue;
                    }

                    // Compute embedding
                    Mat embedding = computeEmbedding(preprocessedFace);
                    preprocessedFace.release();

                    if (!embedding.empty()) {
                        embeddings.add(embedding);
                    }
                }

                if (embeddings.isEmpty()) {
                    attendanceLogger.info("Failed to compute embeddings for " + student.getName());
                    failureCount++;
                    continue;
                }

                // Compute average embedding
                Mat avgEmbedding = computeAverageEmbedding(embeddings);

                // Store the average embedding in FaceData
                faceData.setFaceEmbedding(avgEmbedding);

                // Clean up individual embeddings
                for (Mat emb : embeddings) {
                    emb.release();
                }

                attendanceLogger.info("Trained " + student.getName() +
                        " (" + embeddings.size() + " images)");
                successCount++;

            } catch (Exception e) {
                System.err.println("✗ Error processing " + student.getName() + ": " + e.getMessage());
                failureCount++;
                e.printStackTrace();
            }
        }

        attendanceLogger.info("Training complete: " + successCount + " succeeded, " +
                failureCount + " failed");
    }

    @Override
    public RecognitionResult recognize(Mat faceImage, List<Student> enrolledStudents) {
        if (faceNet == null || faceNet.empty()) {
            System.err.println("Cannot recognize: model not loaded");
            return new RecognitionResult();
        }

        if (faceImage == null || faceImage.empty()) {
            System.err.println("Input face image is empty or null");
            return new RecognitionResult();
        }

        if (enrolledStudents == null || enrolledStudents.isEmpty()) {
            System.err.println("No enrolled students to compare against");
            return new RecognitionResult();
        }

        try {
            // Preprocess the input face
            Rect fullRect = new Rect(0, 0, faceImage.cols(), faceImage.rows());
            Mat preprocessedFace = faceProcessingService.preprocessFace(
                    faceImage,
                    fullRect,
                    INPUT_WIDTH,
                    INPUT_HEIGHT,
                    false);

            if (preprocessedFace.empty()) {
                System.err.println("Failed to preprocess face");
                return new RecognitionResult();
            }

            // Compute embedding for input face
            Mat inputEmbedding = computeEmbedding(preprocessedFace);
            preprocessedFace.release();

            if (inputEmbedding.empty()) {
                System.err.println("Failed to compute embedding for input face");
                return new RecognitionResult();
            }

            // Find best match
            double bestSimilarity = -1.0;
            Student bestMatch = null;

            for (Student student : enrolledStudents) {
                FaceData faceData = student.getFaceData();

                if (faceData == null || faceData.getFaceEmbedding() == null) {
                    continue;
                }

                Mat storedEmbedding = faceData.getFaceEmbedding(); // Using histogram field for embeddings

                if (storedEmbedding.empty()) {
                    continue;
                }

                try {
                    // Compute similarity (using cosine similarity)
                    double similarity = cosineSimilarity(inputEmbedding, storedEmbedding);

                    System.out.println("Comparing with " + student.getName() +
                            " - Input shape: " + inputEmbedding.rows() + "x" + inputEmbedding.cols() +
                            ", Stored shape: " + storedEmbedding.rows() + "x" + storedEmbedding.cols() +
                            ", Similarity: " + String.format("%.4f", similarity));

                    if (similarity > bestSimilarity) {
                        bestSimilarity = similarity;
                        bestMatch = student;
                    }
                } catch (Exception e) {
                    System.err.println("Error comparing with student " + student.getName() + ": " + e.getMessage());
                }
            }

            inputEmbedding.release();

            final double MIN_SIMILARITY_THRESHOLD = -0.70;

            // Return result if meets threshold
            if (bestMatch != null && bestSimilarity >= MIN_SIMILARITY_THRESHOLD) {
                // Convert similarity to percentage (cosine similarity is -1 to 1)

                // Mapping Formula for OpenFace embeddings:
                // - similarity 0.7+ → 70%+ confidence (good match)
                // - similarity 0.6  → 50% confidence (uncertain)
                // - similarity 0.4  → 30% confidence (likely wrong)
                // - similarity 0.2  → 10% confidence (definitely wrong)
                double confidence;
                if (bestSimilarity >= 0.5) {
                    // Good match range: 0.6-1.0 maps to 50%-100%
                    confidence = 50.0 + (bestSimilarity - 0.6) / 0.4 * 50.0;
                } else if (bestSimilarity >= 0.0) {
                    // Uncertain range: 0.0-0.6 maps to 10%-50%
                    confidence = 10.0 + (bestSimilarity / 0.6) * 40.0;
                } else {
                    // Poor match range: -1.0-0.0 maps to 0%-10%
                    confidence = Math.max(0, 10.0 + bestSimilarity * 10.0);
                }
                return new RecognitionResult(bestMatch, confidence);
            }

            return new RecognitionResult();

        } catch (Exception e) {
            System.err.println("Error during recognition: " + e.getMessage());
            e.printStackTrace();
            return new RecognitionResult();
        }
    }

    /**
     * Recognize multiple faces from a list of face images.
     */
    public List<RecognitionResult> recognizeBatch(List<Mat> faceImages, List<Student> enrolledStudents) {
        List<RecognitionResult> results = new ArrayList<>();

        for (Mat faceImage : faceImages) {
            RecognitionResult result = recognize(faceImage, enrolledStudents);
            results.add(result);
        }

        return results;
    }

    // ----- Embedding methods -----
    /**
     * Compute face embedding using OpenFace DNN model.
     * 
     * @param faceImage The preprocessed face image (96x96 RGB)
     * @return 128-dimensional embedding vector as Mat, or empty Mat on failure
     */
    private Mat computeEmbedding(Mat faceImage) {
        if (faceNet == null || faceNet.empty()) {
            System.err.println("Cannot compute embedding: model not loaded");
            return new Mat();
        }

        if (faceImage.empty()) {
            System.err.println("Cannot compute embedding: input face image is empty");
            return new Mat();
        }

        try {
            // Prepare the image for the network
            Mat blob = Dnn.blobFromImage(
                    faceImage,
                    SCALE_FACTOR,
                    new Size(INPUT_WIDTH, INPUT_HEIGHT),
                    MEAN,
                    SWAP_RB,
                    false);

            // Set input and perform forward pass
            faceNet.setInput(blob);
            Mat embedding = faceNet.forward();

            // Clean up blob
            blob.release();

            // Validate output dimensions
            if (embedding.total() != EMBEDDING_SIZE) {
                System.err.println("Unexpected embedding size: " + embedding.total() +
                        " (expected " + EMBEDDING_SIZE + ")");
                embedding.release();
                return new Mat();
            }

            // Normalize the embedding (L2 normalization)
            Mat normalizedEmbedding = normalizeEmbedding(embedding);
            embedding.release();

            // Force consistent shape (1 x 128)
            if (normalizedEmbedding.rows() != 1 || normalizedEmbedding.cols() != 128) {
                Mat reshaped = normalizedEmbedding.reshape(0, 1); // force 1x128
                normalizedEmbedding.release();
                normalizedEmbedding = reshaped;
            }

            System.out.println("Embedding shape: " + normalizedEmbedding.rows() + "x" + normalizedEmbedding.cols());

            return normalizedEmbedding;

        } catch (Exception e) {
            System.err.println("Error computing embedding: " + e.getMessage());
            e.printStackTrace();
            return new Mat();
        }
    }

    /**
     * Compute average of multiple embeddings.
     */
    private Mat computeAverageEmbedding(List<Mat> embeddings) {
        if (embeddings.isEmpty()) {
            return new Mat();
        }

        // Initialize sum with zeros
        Mat sum = Mat.zeros(embeddings.get(0).rows(), embeddings.get(0).cols(), embeddings.get(0).type());

        // Sum all embeddings
        for (Mat embedding : embeddings) {
            org.opencv.core.Core.add(sum, embedding, sum);
        }

        // Compute average
        sum.convertTo(sum, sum.type(), 1.0 / embeddings.size());

        // Normalize the average
        Mat normalized = normalizeEmbedding(sum);
        sum.release();

        return normalized;
    }

    /**
     * Normalize embedding vector using L2 normalization.
     * This ensures consistent distance metrics for face comparison.
     */
    private Mat normalizeEmbedding(Mat embedding) {
        Mat normalized = new Mat();
        double norm = Math.sqrt(embedding.dot(embedding));

        if (norm > 0) {
            embedding.convertTo(normalized, embedding.type(), 1.0 / norm);
        } else {
            normalized = embedding.clone();
        }

        return normalized;
    }

    /**
     * Compute cosine similarity between two embeddings.
     * Returns a value between -1 and 1, where 1 means identical.
     */
    private double cosineSimilarity(Mat embedding1, Mat embedding2) {
        if (embedding1.empty() || embedding2.empty()) {
            return -1.0;
        }

        if (embedding1.total() != embedding2.total()) {
            System.err.println("Embeddings have different dimensions");
            return -1.0;
        }

        // Cosine similarity = dot product of normalized vectors
        double similarity = embedding1.dot(embedding2);

        return similarity;
    }

}