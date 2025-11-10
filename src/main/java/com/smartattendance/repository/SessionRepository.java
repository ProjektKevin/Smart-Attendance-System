package com.smartattendance.repository;

// import com.smartattendance.model.Session;
// import com.smartattendance.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.entity.Session;

public class SessionRepository {

    // Select all sessions 
    public List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT s.session_id, c.course_code, s.late_threshold, s.location, s.start_time, s.end_time, s.session_date, s.status FROM sessions s JOIN courses c ON s.course_id = c.course_id;";

        try (Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Convert SQL types to Java time types
                java.sql.Date sqlDate = rs.getDate("session_date");
                Timestamp sqlStartTime = rs.getTimestamp("start_time");
                Timestamp sqlEndTime = rs.getTimestamp("end_time");
                
                sessions.add(new Session(
                    rs.getInt("session_id"),
                    rs.getString("course_code"),
                    sqlDate != null ? sqlDate.toLocalDate() : null,
                    sqlStartTime != null ? sqlStartTime.toLocalDateTime().toLocalTime() : null,
                    sqlEndTime != null ? sqlEndTime.toLocalDateTime().toLocalTime() : null,
                    rs.getString("location"),
                    rs.getInt("late_threshold"),
                    rs.getString("status")
                ));
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        return sessions;
    }

    // Select closed sessions 
    public List<Session> findClosedSessions() {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT s.session_id, c.course_code, s.late_threshold, s.location, s.start_time, s.end_time, s.session_date, s.status FROM sessions s JOIN courses c ON s.course_id = c.course_id WHERE s.status = ?";

        try (Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "Closed");

            while (rs.next()) {
                // Convert SQL types to Java time types
                java.sql.Date sqlDate = rs.getDate("session_date");
                Timestamp sqlStartTime = rs.getTimestamp("start_time");
                Timestamp sqlEndTime = rs.getTimestamp("end_time");
                
                sessions.add(new Session(
                    rs.getInt("session_id"),
                    rs.getString("course_code"),
                    sqlDate != null ? sqlDate.toLocalDate() : null,
                    sqlStartTime != null ? sqlStartTime.toLocalDateTime().toLocalTime() : null,
                    sqlEndTime != null ? sqlEndTime.toLocalDateTime().toLocalTime() : null,
                    rs.getString("location"),
                    rs.getInt("late_threshold"),
                    rs.getString("status")
                ));
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        return sessions;
    }

    // Select session by id
    public Session findById(int id) {
        String sql = "SELECT s.session_id, c.course_code, s.late_threshold, s.location, s.start_time, s.end_time, s.session_date, s.status FROM sessions s JOIN courses c ON s.course_id = c.course_id WHERE session_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Convert SQL types to Java time types
                    java.sql.Date sqlDate = rs.getDate("session_date");
                    Timestamp sqlStartTime = rs.getTimestamp("start_time");
                    Timestamp sqlEndTime = rs.getTimestamp("end_time");
                    return new Session(
                            rs.getInt("session_id"),
                            rs.getString("course_code"),
                            sqlDate != null ? sqlDate.toLocalDate() : null,
                            sqlStartTime != null ? sqlStartTime.toLocalDateTime().toLocalTime() : null,
                            sqlEndTime != null ? sqlEndTime.toLocalDateTime().toLocalTime() : null,
                            rs.getString("location"),
                            rs.getInt("late_threshold"),
                            rs.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Insert session  
    public void save(Session s) {
        String sql = "INSERT INTO sessions (course_id, late_threshold, location, start_time, end_time, session_date, status) VALUES ((SELECT course_id FROM courses WHERE course_code = ?), ?, ?, ?, ?, ?, ?) ";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Convert LocalDate to java.sql.Date for session_date
            java.sql.Date sqlDate = java.sql.Date.valueOf(s.getSessionDate());
            
            // For timestamp columns, we need to combine session_date with the time
            // Create LocalDateTime objects by combining session_date with the time
            LocalDateTime startDateTime = LocalDateTime.of(s.getSessionDate(), s.getStartTime());
            LocalDateTime endDateTime = LocalDateTime.of(s.getSessionDate(), s.getEndTime());
            
            // Convert to Timestamp
            Timestamp sqlStartTime = Timestamp.valueOf(startDateTime);
            Timestamp sqlEndTime = Timestamp.valueOf(endDateTime);

            ps.setString(1, s.getCourse());
            ps.setInt(2, s.getLateThresholdMinutes());
            ps.setString(3, s.getLocation());
            ps.setTimestamp(4, sqlStartTime);
            ps.setTimestamp(5, sqlEndTime);
            ps.setDate(6, sqlDate);
            ps.setString(7, s.getStatus());

            int affectedRows = ps.executeUpdate();

            // Get the auto-generated session_id
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        s.setSessionId(generatedId); // Update the session object with the new ID
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete session by id
    public void deleteById(int id) {
        String sql = "DELETE FROM sessions where session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
            System.out.println("Session deleted successfully");
            } else {
                System.out.println("No session found with ID: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete all sessions and reset id to start from 1
    public void deleteAll() {
        String deleteSql = "DELETE FROM sessions";
        String resetSql = "ALTER SEQUENCE sessions_session_id_seq RESTART WITH 1";

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Delete all sessions
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                int deletedCount = ps.executeUpdate();
                System.out.println("Deleted " + deletedCount + " sessions");
            }
            
            // Reset auto-increment counter
            try (PreparedStatement ps = conn.prepareStatement(resetSql)) {
                ps.executeUpdate();
                System.out.println("Reset auto-increment counter");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update status of session by id
    public void updateStatus(int id, String status){
        String sql = "UPDATE sessions SET status = ? WHERE session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            
            ps.setString(1, status);
            ps.setInt(2, id);

            ps.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}