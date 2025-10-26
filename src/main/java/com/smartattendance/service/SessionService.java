package com.smartattendance.service;

import com.smartattendance.model.AttendanceRecord;
import com.smartattendance.model.Session;
import com.smartattendance.model.Student;
import com.smartattendance.repository.AttendanceRecordRepository;
import com.smartattendance.repository.SessionRepository;
import com.smartattendance.repository.StudentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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
            AttendanceRecord record = new AttendanceRecord(
                student,
                s,
                "Pending", // Default status
                "-", // Default method
                0.0, // Default confidence
                LocalDateTime.now() // Default LocalDateTime
            );
            record.setNote("Auto-created with session"); // Default note
            
            attendanceRepo.save(record);
        }
    }
}
