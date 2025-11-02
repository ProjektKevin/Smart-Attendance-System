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
        String sql = "SELECT user_id, session_id, note, confidence, marked_at, method, status FROM attendance";

        try (Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Load the actual Student and Session objects
                Student student = studentRepo.findById(rs.getInt("user_id"));
                Session session = sessionRepo.findById(rs.getInt("session_id"));

                AttendanceRecord record = new AttendanceRecord(
                    student,
                    session,
                    rs.getString("status"),
                    rs.getString("method"),
                    rs.getDouble("confidence"),
                    rs.getTimestamp("marked_at").toLocalDateTime()
                );
                
                // Set the note if it exists
                String note = rs.getString("note");
                if (note != null) {
                    record.setNote(note); 
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
        String sql = "SELECT user_id, session_id, note, confidence, marked_at, method, status FROM attendance WHERE session_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = studentRepo.findById(rs.getInt("user_id"));
                    Session session = sessionRepo.findById(rs.getInt("session_id"));

                    AttendanceRecord record = new AttendanceRecord(
                        student,
                        session,
                        rs.getString("status"),
                        rs.getString("method"),
                        rs.getDouble("confidence"),
                        rs.getTimestamp("marked_at").toLocalDateTime()
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
        String sql = "SELECT user_id, session_id, note, confidence, marked_at, method, status FROM attendance WHERE user_id = ? AND session_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setInt(2, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student student = studentRepo.findById(rs.getInt("user_id"));
                    Session session = sessionRepo.findById(rs.getInt("session_id"));

                    AttendanceRecord record = new AttendanceRecord(
                        student,
                        session,
                        rs.getString("status"),
                        rs.getString("method"),
                        rs.getDouble("confidence"),
                        rs.getTimestamp("marked_at").toLocalDateTime()
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
        String sql = "INSERT INTO attendance (user_id, session_id, note, confidence, marked_at, method, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, record.getStudent().getStudentId());
            ps.setInt(2, record.getSession().getSessionId());
            ps.setString(3, record.getNote());
            ps.setDouble(4, record.getConfidence());
            ps.setTimestamp(5, Timestamp.valueOf(record.getTimestamp()));
            ps.setString(6, record.getMethod());
            ps.setString(7, record.getStatus());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean update(AttendanceRecord record) {
        String sql = "UPDATE attendance SET status = ?, method = ?, confidence = ?, timestamp = ?, note = ? WHERE user_id = ? AND session_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, record.getStatus());
            ps.setString(2, record.getMethod());
            ps.setDouble(3, record.getConfidence());
            ps.setTimestamp(4, Timestamp.valueOf(record.getTimestamp()));
            ps.setString(5, record.getNote());
            ps.setInt(6, record.getStudent().getStudentId());
            ps.setInt(7, record.getSession().getSessionId());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateStatus(AttendanceRecord record){
        String sql = "UPDATE attendance SET marked_at = ?, method = 'Manual', status = ? WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
                
            ps.setTimestamp(1, Timestamp.valueOf(record.getTimestamp()));
            ps.setString(2, record.getStatus());
            ps.setInt(3, record.getStudent().getStudentId());
            ps.setInt(4, record.getSession().getSessionId());
            
            ps.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}