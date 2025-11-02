package com.smartattendance.service;

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
import com.smartattendance.repository.AttendanceRecordRepository;
import com.smartattendance.repository.SessionRepository;
import com.smartattendance.repository.StudentRepository;

public class SessionService {
    private final Map<String, Session> sessions = new HashMap<>();
    private final SessionRepository repo;

    public SessionService() {
        this.repo = new SessionRepository();
        // startLifecycleChecker();
    }

    // Return all sessions
    public List<Session> getAllSessions() {
        return repo.findAll();
    }

    /** Find a student by ID, or null if not found. */
    public Session findById(int id) {
        return repo.findById(id);
    }

    public Session createSession(String courseId, LocalDate date, LocalTime start,
                                 LocalTime end, String loc, int lateThreshold) {
        Session session = new Session(courseId.toUpperCase(), date, start, end, loc, lateThreshold);
        repo.save(session);
        createAttendanceRecordsForSession(session);
        return session;
    }

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

    public void deleteSession(int id) {
        repo.deleteById(id);
    }

    public void updateSessionStatus(Session s){
        repo.updateStatus(s.getSessionId(), s.getStatus());
    }

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
                AttendanceStatus.ABSENT, // Default status
                0.0, // Default confidence
                MarkMethod.NONE,      // Default method
                LocalDateTime.now() // Default LocalDateTime
            );
            record.setNote("Auto-created with session"); // Default note
            
            attendanceRepo.save(record);
        }
    }

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
}
