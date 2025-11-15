package com.smartattendance;

import com.smartattendance.config.Config;
import com.smartattendance.controller.AttendanceController;
import com.smartattendance.model.entity.AuthSession;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.AuthService;
import com.smartattendance.service.CourseService;
import com.smartattendance.service.FaceDetectionService;
import com.smartattendance.service.FaceProcessingService;
import com.smartattendance.service.FaceRecognitionService;
import com.smartattendance.service.ImageService;
import com.smartattendance.service.ProfileService;
import com.smartattendance.service.SessionService;
import com.smartattendance.service.StudentService;
import com.smartattendance.service.UserService;
import com.smartattendance.service.recognition.HistogramRecognizer;
import com.smartattendance.service.recognition.OpenFaceRecognizer;
import com.smartattendance.util.AutoAttendanceUpdater;
import com.smartattendance.util.CameraUtils;
import com.smartattendance.util.FileLoader;
import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * Main Application Context where all services are initialised and loaded
 * The created instances are reused across the project avoid extra memo leak or
 * usage
 * 
 * @author Chue Wan Yan, Ernest Lun, Lim Jia Hui, Min Thet Khine, Thiha Swan Htet 
 */
public final class ApplicationContext {

    private static boolean initialized = false;
    private static AuthSession session;
    private static SessionService sessionService;

    // Business Services
    private static AuthService authService;
    private static UserService userService;
    private static StudentService studentService;
    private static AttendanceService attendanceService;
    private static ProfileService profileService;
    private static CourseService courseService;
    private static ImageService imageService;

    // Busines Utils & Controller
    // F_MA: added by felicia handling marking attendance
    private static AutoAttendanceUpdater autoAttendanceUpdater;
    private static AttendanceController attendanceController;

    // OpenCV Services
    private static FaceDetectionService faceDetectionService;
    private static FaceProcessingService faceProcessingService;
    private static FaceRecognitionService faceRecognitionService;

    private static HistogramRecognizer histogramRecognizer;
    private static OpenFaceRecognizer openFaceRecognizer;

    // Logger
    private static final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    /**
     * Initialize the application context.
     * This method initializes all services.
     * 
     * @throws IllegalStateException if already initialized
     */
    public static void initialize() {
        if (initialized) {
            throw new IllegalStateException("ApplicationContext Already Initialized");
        }

        // Set session
        session = new AuthSession();
        appLogger.info("Auth Session Initialized.");
        sessionService = new SessionService();
        appLogger.info("Session Service Initialized.");

        // Load Opencv
        loadOpenCV();

        // Load Opencv Services
        loadOpenCVServices();

        // chore(), William: Add config loading here after implementation

        // Initialize services
        authService = new AuthService();
        appLogger.info("Auth Service Initialized.");

        userService = new UserService();
        appLogger.info("User Service Initialized.");

        studentService = new StudentService();
        appLogger.info("Student Service Initialized.");

        profileService = new ProfileService();
        appLogger.info("Profile Service Initialized.");

        courseService = new CourseService();
        appLogger.info("Course Service Initialized.");

        // F_MA: added by felicia handling marking attendance
        attendanceService = new AttendanceService();
        appLogger.info("Attendance Service Initialized.");

        initialized = true;

        // F_MA: added by felicia handling marking attendance
        // Start auto-attendance updater every 60 seconds
        autoAttendanceUpdater = new AutoAttendanceUpdater(attendanceService);
        autoAttendanceUpdater.addObserver(ApplicationContext.getAttendanceController());
        autoAttendanceUpdater.startAutoUpdate(60);

        // Apply recognition algorithm from the config
        applyRecognitionAlgorithm();
    }

    public static AttendanceController getAttendanceController() {
        return attendanceController;
    }

    public static void loadOpenCV() {
        try {
            // Load opencv locally
            nu.pattern.OpenCV.loadLocally();
            appLogger.info("OpenCV Loaded Successfully.");
        } catch (Exception e) {
            appLogger.error("Error Loading OpenCV", e);
        }
    }

    /**
     * Load OpenCV-related services
     */
    private static void loadOpenCVServices() {
        try {
            double highThreshold = Double.parseDouble(Config.get("recognition.high.threshold"));

            // Cascade file variables
            String cascadePath = FileLoader.loadToTempFile("/haarcascades/haarcascade_frontalface_default.xml");

            // Initialize face detection service
            faceDetectionService = new FaceDetectionService(cascadePath);

            // Initialize face image processing
            faceProcessingService = new FaceProcessingService(faceDetectionService);
            appLogger.info("Image Processing Service Initialized");

            // Initialize both recognizer models
            histogramRecognizer = new HistogramRecognizer(faceProcessingService, highThreshold);
            appLogger.info("Histogram Recognizer Initialized");

            openFaceRecognizer = new OpenFaceRecognizer(faceProcessingService, highThreshold);
            appLogger.info("OpenFace Recognizer Initialized");

            // Initialize face recognition
            faceRecognitionService = new FaceRecognitionService(faceDetectionService);
            appLogger.info("Face Recognition Service Initialized");
            if (!openFaceRecognizer.isModelLoaded()) {
                appLogger.warn("OpenFace Model Not Loaded. Check Model File Path.");
                throw new IllegalStateException("OpenFace Model Required But Failed to Load");
            }
            appLogger.info("Face Recognition Service Initialized");

            // Initialize Image Service
            imageService = new ImageService(faceProcessingService);
            appLogger.info("Image Service Initialized");

            openFaceRecognizer = new OpenFaceRecognizer(faceProcessingService);

        } catch (IllegalStateException e) {
            appLogger.error("OpenFace Unavailable", e);
        } catch (Exception e) {
            appLogger.error("Error Loading OpenCV Services", e);
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
     * Get the SessionService instance.
     *
     * @return SessionService
     * @throws IllegalStateException if not initialized
     */
    public static SessionService getSessionService() {
        checkInitialized();
        return sessionService;
    }

    /**
     * Get the UserService instance.
     *
     * @return UserService
     * @throws IllegalStateException if not initialized
     */
    public static UserService getUserService() {
        checkInitialized();
        return userService;
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
     * Get the ProfileService instance.
     *
     * @return ProfileService
     * @throws IllegalStateException if not initialized
     */
    public static ProfileService getProfileService() {
        checkInitialized();
        return profileService;
    }

    /**
     * Get the CourseService instance.
     *
     * @return CourseService
     * @throws IllegalStateException if not initialized
     */
    public static CourseService getCourseService() {
        checkInitialized();
        return courseService;
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
     * Get the ImageService instance.
     *
     * @return ImageService
     * @throws IllegalStateException if not initialized
     */
    public static ImageService getImageService() {
        checkInitialized();
        return imageService;
    }

    /**
     * Get the HistogramRecognizer instance (DNN-based).
     *
     * @return HistogramRecognizer
     * @throws IllegalStateException if not initialized
     */
    public static HistogramRecognizer getHistogramRecognizer() {
        checkInitialized();
        return histogramRecognizer;
    }

    /**
     * Get the OpenFaceRecognizer instance (DNN-based).
     *
     * @return OpenFaceRecognizer
     * @throws IllegalStateException if not initialized
     */
    public static OpenFaceRecognizer getOpenFaceRecognizer() {
        checkInitialized();
        return openFaceRecognizer;
    }

    /**
     * Get the CameraUtils instance
     *
     * @return CameraUtils instance
     */
    public static CameraUtils getCameraUtils() {
        return CameraUtils.getInstance();
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
     * Apply the configured recognition algorithm to FaceRecognitionService
     */
    public static void applyRecognitionAlgorithm() {
        checkInitialized();

        String algorithm = Config.get("recognition.algorithm");
        if (algorithm == null) {
            algorithm = "HISTOGRAM"; // default
        }

        faceRecognitionService.switchAlgorithm(algorithm);
    }

    /**
     * Shutdown and cleanup resources.
     */
    public static void shutdown() {
        if (!initialized) {
            return;
        }

        // F_MA: added by felicia handling marking attendance
        // Stop auto attendance updater
        if (autoAttendanceUpdater != null) {
            autoAttendanceUpdater.stopAutoUpdate();
        }

        // Release camera resources
        CameraUtils.getInstance().releaseCamera();
        // Log out the user
        session.logout();
        initialized = false;
    }
}
