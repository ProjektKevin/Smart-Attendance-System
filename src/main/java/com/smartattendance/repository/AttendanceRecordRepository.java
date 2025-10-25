package com.smartattendance.repository;

import com.smartattendance.model.AttendanceRecord;
import com.smartattendance.model.Session;
import com.smartattendance.model.Student;
import com.smartattendance.util.DatabaseUtil;
import java.util.*;
import java.sql.*;

public class AttendanceRecordRepository {
    private StudentRepository studentRepo;
    private SessionRepository sessionRepo;
    
    public AttendanceRecordRepository() {
        this.studentRepo = new StudentRepository();
        this.sessionRepo = new SessionRepository();
    }
    
    // select all
    public List<AttendanceRecord> findAll() {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT student_id, session_id, status, method, confidence, timestamp, note FROM attendance_records";

        try (Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Load the actual Student and Session objects
                Student student = studentRepo.findById(rs.getString("student_id"));
                Session session = sessionRepo.findById(rs.getInt("session_id"));

                AttendanceRecord record = new AttendanceRecord(
                    student,
                    session,
                    rs.getString("status"),
                    rs.getString("method"),
                    rs.getDouble("confidence"),
                    rs.getTimestamp("timestamp").toLocalDateTime()
                );
                
                // Set the note if it exists
                String note = rs.getString("note");
                if (note != null) {
                    record.setNote(note); // Assuming you have a setter method
                }
                
                records.add(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    // select by session_id - returns multiple records for a session
    public List<AttendanceRecord> findBySessionId(int sessionId) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT student_id, session_id, status, method, confidence, timestamp, note FROM attendance_records WHERE session_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = studentRepo.findById(rs.getString("student_id"));
                    Session session = sessionRepo.findById(rs.getInt("session_id"));

                    AttendanceRecord record = new AttendanceRecord(
                        student,
                        session,
                        rs.getString("status"),
                        rs.getString("method"),
                        rs.getDouble("confidence"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                    );
                    
                    // Set the note if it exists
                    String note = rs.getString("note");
                    if (note != null) {
                        record.setNote(note);
                    }
                    
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return records;
    }
    
    // If you need to find a single record by composite key (student_id + session_id)
    public AttendanceRecord findById(String studentId, int sessionId) {
        String sql = "SELECT student_id, session_id, status, method, confidence, timestamp, note FROM attendance_records WHERE student_id = ? AND session_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setInt(2, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student student = studentRepo.findById(rs.getString("student_id"));
                    Session session = sessionRepo.findById(rs.getInt("session_id"));

                    AttendanceRecord record = new AttendanceRecord(
                        student,
                        session,
                        rs.getString("status"),
                        rs.getString("method"),
                        rs.getDouble("confidence"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                    );
                    
                    // Set the note if it exists
                    String note = rs.getString("note");
                    if (note != null) {
                        record.setNote(note);
                    }
                    
                    return record;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean save(AttendanceRecord record) {
        String sql = "INSERT INTO attendance_records (student_id, session_id, status, method, confidence, timestamp, note) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, record.getStudent().getStudentId());
            ps.setInt(2, record.getSession().getSessionId());
            ps.setString(3, record.getStatus().toString());
            ps.setString(4, record.getMethod().toString());
            ps.setDouble(5, record.getConfidence());
            ps.setTimestamp(6, Timestamp.valueOf(record.getTimestamp()));
            ps.setString(7, record.getNote());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean update(AttendanceRecord record) {
        String sql = "UPDATE attendance_records SET status = ?, method = ?, confidence = ?, timestamp = ?, note = ? WHERE student_id = ? AND session_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, record.getStatus().toString());
            ps.setString(2, record.getMethod().toString());
            ps.setDouble(3, record.getConfidence());
            ps.setTimestamp(4, Timestamp.valueOf(record.getTimestamp()));
            ps.setString(5, record.getNote());
            ps.setString(6, record.getStudent().getStudentId());
            ps.setInt(7, record.getSession().getSessionId());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}