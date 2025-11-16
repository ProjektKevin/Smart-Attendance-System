package com.smartattendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;
import com.smartattendance.repository.AttendanceRecordRepository;
import com.smartattendance.repository.SessionRepository;
import com.smartattendance.repository.StudentRepository;
import com.smartattendance.service.rules.AutoSessionRule;
import com.smartattendance.service.rules.ConflictPreventionRule;
import com.smartattendance.service.rules.SessionEndedRule;
import com.smartattendance.service.rules.StatusValidationRule;
import com.smartattendance.service.rules.TimeRule;

/**
 * Service class for managing session business logic and operations.
 * 
 * This service acts as a facade between controllers and repositories,
 * implementing business rules, validation, and complex operations related
 * to session management. It utilises the Decorator pattern for automatic
 * session processing rules.
 * </p>
 * 
 * Key responsibilities:
 * - Session lifecycle management (creation, starting, stopping)
 * - Automatic session processing with rule-based validation
 * - Attendance record creation and management
 * - Business rule enforcement and validation
 * - Integration with application context and authentication
 * 
 * @author Lim Jia Hui
 * @version 19:06 16 Nov 2025
 */
public class SessionService {

    // ========== DEPENDENCIES ==========

    /** Repository for session data access operations */
    private final SessionRepository repo;

    /** Rule engine for automatic session processing using Decorator pattern */
    private final AutoSessionRule autoSessionRule;

    // ========== CONSTRUCTOR ==========

    /**
     * Constructs a new SessionService with initialized dependencies.
     * Sets up the rule decorator chain for automatic session processing.
     * 
     * Rule chain order (outermost to innermost):
     * - SessionEndedRule - Checks if session has ended
     * - StatusValidationRule - Validates session status
     * - ConflictPreventionRule - Prevents session conflicts
     * - TimeRule - Validates timing constraints
     */
    public SessionService() {
        this.repo = new SessionRepository();
        // Build the decorator chain with the new rule
        this.autoSessionRule = new SessionEndedRule(
                new StatusValidationRule(
                        new ConflictPreventionRule(
                                new TimeRule(),
                                this)));
    }

    // ========== BASIC DATA ACCESS METHODS ==========

    /**
     * Retrieves all sessions from the database.
     *
     * @return a list of all Session objects, never null
     */
    public List<Session> getAllSessions() {
        return repo.findAll();
    }

    /**
     * Finds a session by its unique identifier.
     *
     * @param id the session ID to search for
     * @return the Session object if found, null otherwise
     * @throws IllegalArgumentException if the ID is not positive
     */
    public Session findById(int id) {
        return repo.findById(id);
    }

    /**
     * Checks if there is currently an open session in the system.
     * The system enforces that only one session can be open at a time.
     *
     * @return true if there is an open session, false otherwise
     */
    public boolean isSessionOpen() {
        return repo.isSessionOpen();
    }

    /**
     * Deletes a session by its unique identifier.
     *
     * @param id the session ID to delete
     * @throws IllegalArgumentException if the ID is not positive
     */
    public void deleteSession(int id) {
        repo.deleteById(id);
    }

    /**
     * Deletes all sessions from the system and resets auto-increment counters.
     * 
     * Warning: This operation is destructive and cannot be undone.
     * Use with extreme caution in production environments.
     */
    public void deleteAll() {
        repo.deleteAll();
    }

    /**
     * Updates the status of a session in the database.
     *
     * @param session the Session object containing the new status
     * @throws IllegalArgumentException if the session is null
     */
    public void updateSessionStatus(Session s) {
        repo.updateStatus(s.getSessionId(), s.getStatus());
    }

    // ========== SESSION CREATION METHODS ==========

    /**
     * Creates attendance records for all students enrolled in the session's course.
     * This method is called automatically when a new session is created.
     *
     * @param session the session for which to create attendance records
     * @throws IllegalArgumentException if the session is null
     */
    private void createAttendanceRecordsForSession(Session s) {
        StudentRepository studentRepo = new StudentRepository();
        AttendanceRecordRepository attendanceRepo = new AttendanceRecordRepository();

        // Get all students in the course
        List<Student> enrolledStudents = studentRepo.findByCourse(s.getCourse());

        for (Student student : enrolledStudents) {
            // F_MA: modified by felicia handling marking attendance
            AttendanceRecord record = new AttendanceRecord(
                    student,
                    s,
                    AttendanceStatus.PENDING, // Default status
                    0.0, // Default confidence
                    MarkMethod.NONE, // Default method
                    LocalDateTime.now() // Default LocalDateTime
            );
            record.setNote("Auto-created with session"); // Default note

            attendanceRepo.save(record);
        }
    }

    /**
     * Creates a new session with the specified parameters and generates attendance
     * records.
     *
     * @param courseId      the course identifier (will be converted to uppercase)
     * @param date          the date of the session
     * @param start         the start time of the session
     * @param end           the end time of the session
     * @param location      the physical location of the session
     * @param lateThreshold the grace period in minutes for late attendance
     * @return the newly created Session object with assigned ID
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    public Session createSession(String courseId, LocalDate date, LocalTime start,
            LocalTime end, String loc, int lateThreshold) {
        Session session = new Session(courseId.toUpperCase(), date, start, end, loc, lateThreshold);
        repo.save(session);
        createAttendanceRecordsForSession(session);
        return session;
    }

    // ========== AUTO SETTINGS MANAGEMENT METHODS ==========

    /**
     * Checks if there are other sessions with auto-start enabled, excluding the
     * specified session.
     *
     * @param excludeSessionId the session ID to exclude from the check
     * @return true if other auto-start sessions exist, false otherwise
     * @throws IllegalArgumentException if excludeSessionId is not positive
     */
    public boolean hasOtherAutoStartSession(int excludeSessionId) {
        return repo.hasOtherAutoStartSession(excludeSessionId);
    }

    /**
     * Checks if there are other sessions with auto-stop enabled, excluding the
     * specified session.
     *
     * @param excludeSessionId the session ID to exclude from the check
     * @return true if other auto-stop sessions exist, false otherwise
     * @throws IllegalArgumentException if excludeSessionId is not positive
     */
    public boolean hasOtherAutoStopSession(int excludeSessionId) {
        return repo.hasOtherAutoStopSession(excludeSessionId);
    }

    /**
     * Updates the automatic start and stop settings for a session.
     *
     * @param sessionId the session ID to update
     * @param autoStart whether automatic start should be enabled
     * @param autoStop  whether automatic stop should be enabled
     * @throws IllegalArgumentException if sessionId is not positive
     */
    public void updateAutoSettings(int sessionId, boolean autoStart, boolean autoStop) {
        repo.updateAutoSettings(sessionId, autoStart, autoStop);
    }

    // ========== SESSION VALIDATION METHODS ==========

    /**
     * Validates and starts a session if all conditions are met.
     *
     * @param session the session to start
     * @throws IllegalStateException    if the session cannot be started due to:
     *                                  - Session status is not "Pending"
     *                                  - Another session is already open
     * @throws IllegalArgumentException if the session is null
     */
    public void startSessionIfValid(Session session) throws IllegalStateException {
        if (!"Pending".equals(session.getStatus())) {
            throw new IllegalStateException("Can only start sessions with 'Pending' status");
        }
        // Check if there's already an open session
        if (isSessionOpen()) {
            throw new IllegalStateException(
                    "There is already an open session. Please close the current open session before starting a new one.");
        }
        startSession(session);
    }

    /**
     * Stops multiple sessions if they are valid for stopping.
     * Only stops sessions that are not already closed.
     *
     * @param sessions the list of sessions to stop
     * @return the number of sessions successfully stopped
     * @throws IllegalArgumentException if the sessions list is null
     */
    public int stopSessionsIfValid(List<Session> sessions) {
        int stoppedCount = 0;
        for (Session session : sessions) {
            if (!"Closed".equals(session.getStatus())) {
                stopSession(session);
                stoppedCount++;
            }
        }
        return stoppedCount;
    }

    // ========== BUSINESS OPERATIONS METHODS ==========

    /**
     * Starts a session and updates the application context.
     * This method performs the actual session start operation.
     *
     * @param session the session to start
     * @throws IllegalArgumentException if the session is null
     */
    public void startSession(Session s) {
        s.open();
        repo.updateStatus(s.getSessionId(), s.getStatus());
        ApplicationContext.getAuthSession().setActiveSessionId(s.getSessionId());
    }

    /**
     * Stops a session and clears the active session from application context if
     * applicable.
     *
     * @param session the session to stop
     * @throws IllegalArgumentException if the session is null
     */
    public void stopSession(Session s) {
        s.close();
        repo.updateStatus(s.getSessionId(), s.getStatus());

        // Clear Session ID from AuthSession
        Integer activeSessionId = ApplicationContext.getAuthSession().getActiveSessionId();
        if (activeSessionId != null && activeSessionId == s.getSessionId()) {
            ApplicationContext.getAuthSession().clearActiveSessionId();
        }
    }

    // ========== AUTO-SESSION VALIDATION METHODS ==========

    /**
     * Determines if a session can have auto-start enabled based on business rules.
     * 
     * Auto-start is ONLY allowed for:
     * - Pending sessions that have not ended yet
     * - No other auto-start sessions exist
     * - No open sessions exist (regardless of auto-start status)
     *
     * @param session the session to check
     * @return true if the session can have auto-start enabled, false otherwise
     * @throws IllegalArgumentException if the session is null
     */
    public boolean canSessionHaveAutoStart(Session session) {
        boolean canHave = "Pending".equals(session.getStatus()) &&
                !hasSessionEnded(session) &&
                !hasOtherAutoStartSession(session.getSessionId()) &&
                !isSessionOpen();
        return canHave;
    }

    /**
     * Determines if a session can have auto-stop enabled based on business rules.
     * 
     * Auto-stop is allowed for:
     * - Pending sessions (can auto-stop if they become open and then end)
     * - Open sessions (can auto-stop when time reaches end)
     * - NOT allowed for Closed sessions
     *
     * @param session the session to check
     * @return true if the session can have auto-stop enabled, false otherwise
     * @throws IllegalArgumentException if the session is null
     */
    public boolean canSessionHaveAutoStop(Session session) {
        boolean canHave = !"Closed".equals(session.getStatus());
        return canHave;
    }

    /**
     * Checks if a session has ended based on current time and session end time.
     *
     * @param session the session to check
     * @return true if the current time is after or equal to the session end time,
     *         false otherwise
     * @throws IllegalArgumentException if the session is null
     */
    public boolean hasSessionEnded(Session session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.of(session.getSessionDate(), session.getEndTime());

        boolean hasEnded = now.isAfter(endDateTime) || now.equals(endDateTime);
        return hasEnded;
    }

    // ========== DECORATOR RULE ACCESS METHODS ==========

    /**
     * Checks if a session can be automatically started based on all business rules.
     * Delegates to the rule decorator chain for comprehensive validation.
     *
     * @param session the session to check
     * @return true if the session can be auto-started, false otherwise
     * @throws IllegalArgumentException if the session is null
     */
    public boolean canAutoStart(Session session) {
        boolean result = autoSessionRule.canAutoStart(session);
        return result;
    }

    /**
     * Checks if a session can be automatically stopped based on all business rules.
     * Delegates to the rule decorator chain for comprehensive validation.
     *
     * @param session the session to check
     * @return true if the session can be auto-stopped, false otherwise
     * @throws IllegalArgumentException if the session is null
     */
    public boolean canAutoStop(Session session) {
        boolean result = autoSessionRule.canAutoStop(session);
        return result;
    }

    // ========== AUTO SESSION PROCESSING ==========

    /**
     * Processes all sessions for automatic start and stop operations.
     * This method is typically called by a scheduled background task.
     * 
     * For each session:
     * - Auto-starts sessions with auto_start enabled that pass all rule validations
     * - Auto-stops sessions with auto_stop enabled that pass all rule validations
     */
    public void processAutoSessions() {
        System.out.println("SessionService: Processing auto sessions (background)");
        List<Session> sessions = getAllSessions();

        for (Session session : sessions) {

            // Auto start logic - ONLY for sessions with auto_start = TRUE
            if (session.isAutoStart() && autoSessionRule.canAutoStart(session)) {
                session.open();
                updateSessionStatus(session);
                ApplicationContext.getAuthSession().setActiveSessionId(session.getSessionId());
            }

            // Auto stop logic - ONLY for sessions with auto_stop = TRUE
            if (session.isAutoStop() && autoSessionRule.canAutoStop(session)) {
                session.close();
                updateSessionStatus(session);
            }
        }
    }

    // ========== DEBUGGING AND DIAGNOSTIC METHODS ==========

    /**
     * Retrieves a description of the current auto-session rule configuration.
     * Useful for debugging and understanding the rule evaluation chain.
     *
     * @return a string describing the rule decorator chain
     */
    public String getAutoSessionRulesDescription() {
        String description = autoSessionRule.getRuleDescription();
        return description;
    }
}
