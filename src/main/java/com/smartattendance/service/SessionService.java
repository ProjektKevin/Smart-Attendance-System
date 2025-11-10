package com.smartattendance.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.AttendanceStatus;
import com.smartattendance.model.entity.MarkMethod;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.repository.AttendanceRecordRepository;
import com.smartattendance.repository.SessionRepository;
import com.smartattendance.repository.StudentRepository;

// public class SessionService {
//     private final Map<String, Session> sessions = new HashMap<>();

public class SessionService {
    private final SessionRepository repo;

    public SessionService() {
        this.repo = new SessionRepository();
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
    public void deleteAll(){
        repo.deleteAll();
    }

    // Update session status
    public void updateSessionStatus(Session s){
        repo.updateStatus(s.getSessionId(), s.getStatus());
    }

    // Create Attendance Record for each student enrolled under the session created based on matching course
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
                MarkMethod.NONE,      // Default method
                LocalDateTime.now() // Default LocalDateTime
            );
            record.setNote("Auto-created with session"); // Default note
            
            attendanceRepo.save(record);
        }
    }
}
