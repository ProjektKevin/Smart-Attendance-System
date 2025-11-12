package com.smartattendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
        System.out.println("SessionService: Initialized with rule chain: " + autoSessionRule.getRuleDescription());
    }

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

    // Creation of session using form
    public Session createSession(String courseId, LocalDate date, LocalTime start,
            LocalTime end, String loc, int lateThreshold) {
        Session session = new Session(courseId.toUpperCase(), date, start, end, loc, lateThreshold);
        repo.save(session);
        createAttendanceRecordsForSession(session);
        return session;
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
        // System.out.println("SessionService: updateSessionStatus called for session " + s.getSessionId() +
        //         " with status: " + s.getStatus());
        repo.updateStatus(s.getSessionId(), s.getStatus());
        // System.out.println("SessionService: updateSessionStatus completed for session " + s.getSessionId());
    }

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

    // // Check if there is already a session with auto_start (either 0 or 1)
    // public boolean hasAutoStartSession() {
    //     return repo.hasAutoStartSession();
    // }

    // Check if there are other sessions with auto_start = TRUE excluding the given
    // session
    public boolean hasOtherAutoStartSession(int excludeSessionId) {
        return repo.hasOtherAutoStartSession(excludeSessionId);
    }

    // // Check if there is already a session with auto_stop (either 0 or 1)
    // public boolean hasAutoStopSession() {
    //     return repo.hasAutoStopSession();
    // }

    // Check if there are other sessions with auto_stop = TRUE excluding the given
    // session
    public boolean hasOtherAutoStopSession(int excludeSessionId) {
        return repo.hasOtherAutoStopSession(excludeSessionId);
    }

    // Update auto start/stop settings
    public void updateAutoSettings(int sessionId, boolean autoStart, boolean autoStop) {
        repo.updateAutoSettings(sessionId, autoStart, autoStop);
    }

    // // Method for validation - auto start
    // public String validateAutoStart(Session session) {
    //     System.out.println("SessionService: Validating auto-start for session " + session.getSessionId());

    //     // Check 1: Only one auto-start session allowed
    //     if (hasAutoStartSession()) {
    //         System.out.println("SessionService: Validation failed - another auto-start session exists");
    //         return "Only one session can have Auto Start enabled. Please disable the other session first.";
    //     }

    //     // Check 2: Only Pending sessions can have auto-start enabled
    //     if (!"Pending".equals(session.getStatus())) {
    //         System.out.println("SessionService: Validation failed - session status is " + session.getStatus());
    //         return "Can only enable Auto Start for Pending sessions. Current status: " + session.getStatus();
    //     }

    //     // Check 3: Cannot enable auto-start if session has already ended
    //     if (hasSessionEnded(session)) {
    //         System.out.println("SessionService: Validation failed - session has already ended");
    //         return "Cannot enable Auto Start for sessions that have already ended.";
    //     }

    //     return null; // Validation passed
    // }

    // // Method for validation - auto stop
    // public String validateAutoStop(Session session) {
    //     System.out.println("SessionService: Validating auto-start for session " + session.getSessionId());

    //     // Check 1: Only one auto-stop session allowed
    //     if (hasAutoStopSession()) {
    //         System.out.println("SessionService: Validation failed - another auto-start session exists");
    //         return "Only one session can have Auto Stop enabled. Please disable the other session first.";
    //     }

    //     // Check 2: Only Pending and Open sessions can have auto-stop enabled
    //     if ("Closed".equals(session.getStatus())) {
    //         System.out.println("SessionService: Validation failed - session status is " + session.getStatus());
    //         return "Can only enable Auto Stop for Pending and Open sessions. Current status: " + session.getStatus();
    //     }

    //     return null; // Validation passed
    // }

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
        // System.out.println("SessionService: Session " + session.getSessionId() +
        //         " can have auto-start: " + canHave +
        //         " (Status: " + session.getStatus() +
        //         ", Ended: " + hasSessionEnded(session) +
        //         ", Other Auto-start: " + hasOtherAutoStartSession(session.getSessionId()) +
        //         ", Open Session: " + isSessionOpen() + ")");
        return canHave;
    }

    // Check if session can have auto-stop enabled (for UI)
    public boolean canSessionHaveAutoStop(Session session) {
        // Auto-stop is allowed for:
        // - Pending sessions (can auto-stop if they become open and then end)
        // - Open sessions (can auto-stop when time reaches end)
        // - NOT allowed for Closed sessions
        boolean canHave = !"Closed".equals(session.getStatus());
        // System.out.println("SessionService: Session " + session.getSessionId() +
        //         " can have auto-stop: " + canHave + " (Status: " + session.getStatus() + ")");
        return canHave;
    }

    // Get reason why auto-start is disabled (for logging)
    // public String getAutoStartDisabledReason(Session session) {
    //     if ("Closed".equals(session.getStatus())) w{
    //         return "Auto Start not available for closed sessions";
    //     } else if ("Open".equals(session.getStatus())) {
    //         return "Auto Start not available for open sessions";
    //     } else if (hasSessionEnded(session)) {
    //         return "Auto Start not available - session has ended";
    //     } else if (hasOtherAutoStartSession(session.getSessionId())) {
    //         return "Auto Start not available - another session has auto-start enabled";
    //     } else if (isSessionOpen()) {
    //         return "Auto Start not available - there is already an open session";
    //     }
    //     return "Auto Start not available";
    // }

    // Check if session has ended
    public boolean hasSessionEnded(Session session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.of(session.getSessionDate(), session.getEndTime());

        boolean hasEnded = now.isAfter(endDateTime) || now.equals(endDateTime);
        // System.out.println("SessionService: Checking if session " + session.getSessionId() + " has ended - Current: " +
        //         now + ", End: " + endDateTime + " - " + (hasEnded ? "ENDED" : "NOT ENDED"));
        return hasEnded;
    }

    // ========== DECORATOR RULE ACCESS METHODS ==========

    // Helper methods for UI to check what the decorator rules allow
    public boolean canAutoStart(Session session) {
        boolean result = autoSessionRule.canAutoStart(session);
        // System.out.println("SessionService: canAutoStart for session " + session.getSessionId() +
        //         " (status: " + session.getStatus() + ") = " + result);
        return result;
    }

    public boolean canAutoStop(Session session) {
        boolean result = autoSessionRule.canAutoStop(session);
        // System.out.println("SessionService: canAutoStop for session " + session.getSessionId() +
        //         " (status: " + session.getStatus() + ") = " + result);
        return result;
    }

    // ========== AUTO SESSION PROCESSING ==========

    public void processAutoSessions() {
        System.out.println("SessionService: Processing auto sessions (background)");
        List<Session> sessions = getAllSessions();

        // System.out.println("SessionService: Using rule chain: " + autoSessionRule.getRuleDescription());

        for (Session session : sessions) {
            // System.out.println("SessionService: Checking session " + session.getSessionId() +
            //         " - autoStart: " + session.isAutoStart() + ", autoStop: " + session.isAutoStop() +
            //         ", status: " + session.getStatus());

            // Auto start logic - ONLY for sessions with auto_start = TRUE
            if (session.isAutoStart() && autoSessionRule.canAutoStart(session)) {
                System.out.println("SessionService: AUTO-STARTING session " + session.getSessionId());
                session.open();
                updateSessionStatus(session);
            }

            // Auto stop logic - ONLY for sessions with auto_stop = TRUE
            if (session.isAutoStop() && autoSessionRule.canAutoStop(session)) {
                System.out.println("SessionService: AUTO-STOPPING session " + session.getSessionId());
                session.close();
                updateSessionStatus(session);
            }
        }
        // System.out.println("SessionService: Finished processing auto sessions");
    }

    // Method to get rule description for debugging
    public String getAutoSessionRulesDescription() {
        String description = autoSessionRule.getRuleDescription();
        // System.out.println("SessionService: Current auto session rules: " + description);
        return description;
    }
}
