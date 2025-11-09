package com.smartattendance;

import com.smartattendance.model.entity.AuthSession;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.AuthService;
import com.smartattendance.service.CourseService;
import com.smartattendance.service.FaceDetectionService;
import com.smartattendance.service.FaceProcessingService;
import com.smartattendance.service.FaceRecognitionService;
import com.smartattendance.service.ProfileService;
import com.smartattendance.service.StudentService;
import com.smartattendance.service.UserService;
import com.smartattendance.service.recognition.OpenFaceRecognizer;
import com.smartattendance.util.AutoAttendanceUpdater;
import com.smartattendance.util.FileLoader;
import com.smartattendance.util.security.LoggerUtil;

public final class ApplicationContext {

    private static boolean initialized = false;
    private static AuthSession session;

    // Business Services
    private static AuthService authService;
    private static UserService userService;
    private static StudentService studentService;
    private static AttendanceService attendanceService;
    private static ProfileService profileService;
    private static CourseService courseService;

    // Busines Utils & Controller
    // F_MA: added by felicia handling marking attendance
    private static AutoAttendanceUpdater autoAttendanceUpdater;
    // private static AttendanceController attendanceController;

    // OpenCV Services
    private static FaceDetectionService faceDetectionService;
    private static FaceProcessingService faceProcessingService;
    private static FaceRecognitionService faceRecognitionService;

    private static OpenFaceRecognizer openFaceRecognizer;

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

        // Initialize services
        authService = new AuthService();
        userService = new UserService();
        studentService = new StudentService();
        attendanceService = new AttendanceService();
        profileService = new ProfileService();
        courseService = new CourseService();
        // F_MA: added by felicia handling marking attendance
        attendanceService = new AttendanceService();

        // F_MA: added by felicia handling marking attendance
        // Start auto-attendance updater every 60 seconds
        autoAttendanceUpdater = new AutoAttendanceUpdater(attendanceService);
        // autoAttendanceUpdater.addObserver(ApplicationContext.getAttendanceController());
        autoAttendanceUpdater.startAutoUpdate(60);

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

            openFaceRecognizer = new OpenFaceRecognizer(faceProcessingService);

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

        // F_MA: added by felicia handling marking attendance
        // Stop auto attendance updater
        if (autoAttendanceUpdater != null) {
            autoAttendanceUpdater.stopAutoUpdate();
        }

        // chore(), Harry: Add cleanup logic here (close database connections, release
        // resources, etc.)
        session.logout();
        initialized = false;
    }
}
