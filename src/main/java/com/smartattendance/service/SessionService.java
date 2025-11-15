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

public class SessionService {
    private final SessionRepository repo;
    private final AutoSessionRule autoSessionRule;

    public SessionService() {
        this.repo = new SessionRepository();
        // Build the decorator chain with the new rule
        this.autoSessionRule = new SessionEndedRule(
                new StatusValidationRule(
                        new ConflictPreventionRule(
                                new TimeRule(),
                                this)));
    }

    // ========== BASIC DATA ACCESS ==========

    // Return all sessions
    public List<Session> getAllSessions() {
        return repo.findAll();
    }

    // Find a student by ID
    public Session findById(int id) {
        return repo.findById(id);
    }

    // Check if there is already a session opened
    public boolean isSessionOpen() {
        return repo.isSessionOpen();
    }

    // Deletion of session by id
    public void deleteSession(int id) {
        repo.deleteById(id);
    }

    // Delete all sessions
    public void deleteAll() {
        repo.deleteAll();
    }

    // Update session status
    public void updateSessionStatus(Session s) {
        repo.updateStatus(s.getSessionId(), s.getStatus());
    }

    // ========== SESSION CREATION ==========

    // Create Attendance Record for each student enrolled under the session created
    // based on matching course
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

    // ========== AUTO SETTINGS ==========

    // Check if there are other sessions with auto_start = TRUE excluding the given
    // session
    public boolean hasOtherAutoStartSession(int excludeSessionId) {
        return repo.hasOtherAutoStartSession(excludeSessionId);
    }

    // Check if there are other sessions with auto_stop = TRUE excluding the given
    // session
    public boolean hasOtherAutoStopSession(int excludeSessionId) {
        return repo.hasOtherAutoStopSession(excludeSessionId);
    }

    // Update auto start/stop settings
    public void updateAutoSettings(int sessionId, boolean autoStart, boolean autoStop) {
        repo.updateAutoSettings(sessionId, autoStart, autoStop);
    }

    // Creation of session using form
    public Session createSession(String courseId, LocalDate date, LocalTime start,
            LocalTime end, String loc, int lateThreshold) {
        Session session = new Session(courseId.toUpperCase(), date, start, end, loc, lateThreshold);
        repo.save(session);
        createAttendanceRecordsForSession(session);
        return session;
    }

    // ========== VALIDATE START AND STOP ==========
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

    // ========== BUSINESS OPERATIONS ==========
    public void startSession(Session s) {
        s.open();
        repo.updateStatus(s.getSessionId(), s.getStatus());
        ApplicationContext.getAuthSession().setActiveSessionId(s.getSessionId());
    }

    public void stopSession(Session s) {
        s.close();
        repo.updateStatus(s.getSessionId(), s.getStatus());

        // Clear Session ID from AuthSession
        Integer activeSessionId = ApplicationContext.getAuthSession().getActiveSessionId();
        if (activeSessionId != null && activeSessionId == s.getSessionId()) {
            ApplicationContext.getAuthSession().clearActiveSessionId();
        }
    }

    // ========== VALIDATE AUTO START & AUTO STOP ==========

    // Check if session can have auto-start enabled (for UI)
    public boolean canSessionHaveAutoStart(Session session) {
        // Auto-start is ONLY allowed for:
        // - Pending sessions that haven't ended yet
        // - No other auto-start sessions exist
        // - No open sessions exist (regardless of auto-start status)
        boolean canHave = "Pending".equals(session.getStatus()) &&
                !hasSessionEnded(session) &&
                !hasOtherAutoStartSession(session.getSessionId()) &&
                !isSessionOpen();
        return canHave;
    }

    // Check if session can have auto-stop enabled (for UI)
    public boolean canSessionHaveAutoStop(Session session) {
        // Auto-stop is allowed for:
        // - Pending sessions (can auto-stop if they become open and then end)
        // - Open sessions (can auto-stop when time reaches end)
        // - NOT allowed for Closed sessions
        boolean canHave = !"Closed".equals(session.getStatus());
        return canHave;
    }

    // Check if session has ended
    public boolean hasSessionEnded(Session session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.of(session.getSessionDate(), session.getEndTime());

        boolean hasEnded = now.isAfter(endDateTime) || now.equals(endDateTime);
        return hasEnded;
    }

    // ========== DECORATOR RULE ACCESS METHODS ==========

    // Helper methods for UI to check what the decorator rules allow
    public boolean canAutoStart(Session session) {
        boolean result = autoSessionRule.canAutoStart(session);
        return result;
    }

    public boolean canAutoStop(Session session) {
        boolean result = autoSessionRule.canAutoStop(session);
        return result;
    }

    // ========== AUTO SESSION PROCESSING ==========

    public void processAutoSessions() {
        System.out.println("SessionService: Processing auto sessions (background)");
        List<Session> sessions = getAllSessions();

        for (Session session : sessions) {

            // Auto start logic - ONLY for sessions with auto_start = TRUE
            if (session.isAutoStart() && autoSessionRule.canAutoStart(session)) {
                System.out.println("SessionService: AUTO-STARTING session " + session.getSessionId());
                session.open();
                updateSessionStatus(session);
                ApplicationContext.getAuthSession().setActiveSessionId(session.getSessionId());
            }

            // Auto stop logic - ONLY for sessions with auto_stop = TRUE
            if (session.isAutoStop() && autoSessionRule.canAutoStop(session)) {
                System.out.println("SessionService: AUTO-STOPPING session " + session.getSessionId());
                session.close();
                updateSessionStatus(session);
            }
        }
    }

    // ========== DEBUGGING METHODS ==========

    // Method to get rule description for debugging
    public String getAutoSessionRulesDescription() {
        String description = autoSessionRule.getRuleDescription();
        return description;
    }
}
