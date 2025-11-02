package com.smartattendance.service;

<<<<<<< HEAD
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartattendance.model.AttendanceRecord;
import com.smartattendance.model.AttendanceStatus;
import com.smartattendance.model.MarkMethod;
import com.smartattendance.model.Session;
import com.smartattendance.model.Student;
=======
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
>>>>>>> origin/dev
import com.smartattendance.repository.AttendanceRecordRepository;
import com.smartattendance.repository.SessionRepository;
import com.smartattendance.repository.StudentRepository;

<<<<<<< HEAD
public class SessionService {
    private final Map<String, Session> sessions = new HashMap<>();
=======
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class SessionService {
>>>>>>> origin/dev
    private final SessionRepository repo;

    public SessionService() {
        this.repo = new SessionRepository();
<<<<<<< HEAD
        // startLifecycleChecker();
=======
>>>>>>> origin/dev
    }

    // Return all sessions
    public List<Session> getAllSessions() {
        return repo.findAll();
    }

<<<<<<< HEAD
    /** Find a student by ID, or null if not found. */
=======
    // Find a student by ID
>>>>>>> origin/dev
    public Session findById(int id) {
        return repo.findById(id);
    }

<<<<<<< HEAD
=======
    // Creation of session using form
>>>>>>> origin/dev
    public Session createSession(String courseId, LocalDate date, LocalTime start,
                                 LocalTime end, String loc, int lateThreshold) {
        Session session = new Session(courseId.toUpperCase(), date, start, end, loc, lateThreshold);
        repo.save(session);
        createAttendanceRecordsForSession(session);
        return session;
    }

<<<<<<< HEAD
    public void createSession(Session s) {
        repo.save(s);
    }

    public boolean deleteSession(String id) {
        Session s = sessions.get(id);
        if (s != null && !s.isOpen()) {
            sessions.remove(id);
            return true;
        }
        return false; // can't delete active session
    }

=======
    // Deletion of session by id
>>>>>>> origin/dev
    public void deleteSession(int id) {
        repo.deleteById(id);
    }

<<<<<<< HEAD
=======
    // Update session status
>>>>>>> origin/dev
    public void updateSessionStatus(Session s){
        repo.updateStatus(s.getSessionId(), s.getStatus());
    }

<<<<<<< HEAD
=======
    // Create Attendance Record for each student enrolled under the session created based on matching course
>>>>>>> origin/dev
    private void createAttendanceRecordsForSession(Session s) {
        StudentRepository studentRepo = new StudentRepository();
        AttendanceRecordRepository attendanceRepo = new AttendanceRecordRepository();
        
        // Get all students in the course
        List<Student> enrolledStudents = studentRepo.findByCourse(s.getCourse());
        
        for (Student student : enrolledStudents) {
<<<<<<< HEAD
            // F_MA: modified by felicia handling marking attendance
            AttendanceRecord record = new AttendanceRecord(
                student,
                s,
                AttendanceStatus.ABSENT, // Default status
                0.0, // Default confidence
                MarkMethod.NONE,      // Default method
=======
            AttendanceRecord record = new AttendanceRecord(
                student,
                s,
                "Pending", // Default status
                "-", // Default method
                0.0, // Default confidence
>>>>>>> origin/dev
                LocalDateTime.now() // Default LocalDateTime
            );
            record.setNote("Auto-created with session"); // Default note
            
            attendanceRepo.save(record);
        }
    }
<<<<<<< HEAD

    // private void startLifecycleChecker() {
    //     Timeline lifecycleChecker = new Timeline(
    //             new KeyFrame(Duration.seconds(30), e -> {
    //                 LocalDateTime now = LocalDateTime.now();

    //                 for (Session s : repo.findAll()) {
    //                     LocalDateTime start = LocalDateTime.of(s.getSessionDate(), s.getStartTime());
    //                     LocalDateTime end = LocalDateTime.of(s.getSessionDate(), s.getEndTime());

    //                     // Session should open if within time range
    //                     if (now.isAfter(start) && now.isBefore(end)
    //                             && s.getStatusEnum() != Session.SessionStatus.OPEN) {
    //                         s.open();
    //                         System.out.println("[Auto OPEN] Session " + s.getSessionId());
    //                     }
    //                     // Session should close if time has passed
    //                     else if (now.isAfter(end) && s.getStatusEnum() != Session.SessionStatus.CLOSED) {
    //                         s.close();
    //                         System.out.println("[Auto CLOSE] Session " + s.getSessionId());
    //                     }
    //                     // Otherwise keep pending
    //                     else if (now.isBefore(start) && s.getStatusEnum() != Session.SessionStatus.PENDING) {
    //                         s.setPending();
    //                         System.out.println("[Pending] Session " + s.getSessionId());
    //                     }
    //                 }
    //             }));
    //     lifecycleChecker.setCycleCount(Timeline.INDEFINITE);
    //     lifecycleChecker.play();
    // }
=======
>>>>>>> origin/dev
}
