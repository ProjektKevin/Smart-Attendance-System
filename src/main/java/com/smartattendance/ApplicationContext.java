package com.smartattendance;

import com.smartattendance.model.AuthSession;
import com.smartattendance.service.AuthService;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.FaceDetectionService;
import com.smartattendance.service.FaceProcessingService;
import com.smartattendance.service.FaceRecognitionService;
import com.smartattendance.service.StudentService;

import com.smartattendance.repository.PostgresUserRepository;

import com.smartattendance.util.FileLoader;
import com.smartattendance.util.LoggerUtil;

public final class ApplicationContext {

    private static boolean initialized = false;
    private static AuthSession session;

    // Business Services
    private static AuthService authService;
    private static StudentService studentService;
    private static AttendanceService attendanceService;

    // DB Repositories
    private static PostgresUserRepository userRepository;

    // OpenCV Services
    private static FaceDetectionService faceDetectionService;
    private static FaceProcessingService faceProcessingService;
    private static FaceRecognitionService faceRecognitionService;

    /**
     * Initialize the application context.
     * This method initializes all services.
     * 
     * @throws IllegalStateException if already initialized
     */
    public static void initialize() {
        if (initialized) {
            throw new IllegalStateException("ApplicationContext already initialized");
        }

        // Set session
        session = new AuthSession();

        // Load Opencv
        loadOpenCV();

        // Load Opencv Services
        loadOpenCVServices();

        // chore(), William: Add config loading here after implementation

        // chore(), William: Add database initialization here after implementation

        // chore(), All: Add repositories here after implementation
        userRepository = new PostgresUserRepository();

        // Initialize services
        authService = new AuthService(userRepository);
        studentService = new StudentService();
        attendanceService = new AttendanceService();

        initialized = true;
    }

    public static void loadOpenCV() {
        try {
            // Load opencv locally
            nu.pattern.OpenCV.loadLocally();
            LoggerUtil.LOGGER.info("OpenCV Loaded Successfully.");
        } catch (Exception e) {
            // chore(), Harry: Change back to logger with a different log level
            System.out.println("Error loading opencv: " + e.getMessage());
            // chore(), Harry: Add custom throw error or built in error
        }
    }

    /**
     * Load OpenCV-related services
     */
    private static void loadOpenCVServices() {
        try {
            // Cascade file variables
            String cascadePath = FileLoader.loadToTempFile("/haarcascades/haarcascade_frontalface_default.xml");

            // Initialize face detection service
            faceDetectionService = new FaceDetectionService(cascadePath);

            // Initialize face image processing
            faceProcessingService = new FaceProcessingService(faceDetectionService);
            LoggerUtil.LOGGER.info("Image processing service initialized");

            // Initialize face recognition
            faceRecognitionService = new FaceRecognitionService(faceDetectionService);
            LoggerUtil.LOGGER.info("Face recognition service initialized");

        } catch (Exception e) {
            // chore(), Harry: Change back to logger with a different log level
            System.out.println("Error loading opencv: " + e.getMessage());
            // chore(), Harry: Add custom throw error or built in error
        }
    }

    /**
     * Get the AuthSessioon instance.
     *
     * @return AuthSession
     * @throws IllegalStateException if not initialized
     */
    public static AuthSession getAuthSession() {
        checkInitialized();
        return session;
    }

    /**
     * Get the AuthService instance.
     *
     * @return AuthService
     * @throws IllegalStateException if not initialized
     */
    public static AuthService getAuthService() {
        checkInitialized();
        return authService;
    }

    /**
     * Get the StudentService instance.
     *
     * @return StudentService
     * @throws IllegalStateException if not initialized
     */
    public static StudentService getStudentService() {
        checkInitialized();
        return studentService;
    }

    /**
     * Get the AttendanceService instance.
     *
     * @return AttendanceService
     * @throws IllegalStateException if not initialized
     */
    public static AttendanceService getAttendanceService() {
        checkInitialized();
        return attendanceService;
    }

    /**
     * Get the FaceDetectionService instance.
     *
     * @return FaceDetectionService
     * @throws IllegalStateException if not initialized
     */
    public static FaceDetectionService getFaceDetectionService() {
        checkInitialized();
        return faceDetectionService;
    }

    /**
     * Get the FaceProcessingService instance.
     *
     * @return FaceProcessingService
     * @throws IllegalStateException if not initialized
     */
    public static FaceProcessingService getFaceProcessingService() {
        checkInitialized();
        return faceProcessingService;
    }

    /**
     * Get the FaceRecognitionService instance.
     *
     * @return FaceRecognitionService
     * @throws IllegalStateException if not initialized
     */
    public static FaceRecognitionService getFaceRecognitionService() {
        checkInitialized();
        return faceRecognitionService;
    }

    /**
     * Check if ApplicationContext has been initialized.
     *
     * @throws IllegalStateException if not initialized
     */
    private static void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                    "ApplicationContext not initialized.");
        }
    }

    /**
     * Shutdown and cleanup resources.
     */
    public static void shutdown() {
        if (!initialized) {
            return;
        }

        // chore(), Harry: Add cleanup logic here (close database connections, release
        // resources, etc.)
        session.logout();
        initialized = false;
    }
}
