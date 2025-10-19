package com.smartattendance.repository;

import com.smartattendance.model.Session;
import com.smartattendance.model.Student;
import com.smartattendance.util.DatabaseUtil;
import java.util.*;
import java.sql.*;

public class SessionRepository {

    // select all
    public List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT session_id, course_name, session_date, start_time, end_time, location, late_threshold_minutes, status FROM sessions";

        try (Connection conn = DatabaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Convert SQL types to Java time types
                java.sql.Date sqlDate = rs.getDate("session_date");
                java.sql.Time sqlStartTime = rs.getTime("start_time");
                java.sql.Time sqlEndTime = rs.getTime("end_time");
                
                sessions.add(new Session(
                    rs.getInt("session_id"),
                    rs.getString("course_name"),
                    sqlDate != null ? sqlDate.toLocalDate() : null,
                    sqlStartTime != null ? sqlStartTime.toLocalTime() : null,
                    sqlEndTime != null ? sqlEndTime.toLocalTime() : null,
                    rs.getString("location"),
                    rs.getInt("late_threshold_minutes"),
                    rs.getString("status")
                ));
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        return sessions;
    }

    // select by id
    public Session findById(int id) {
        String sql = "SELECT session_id, course_name, session_date, start_time, end_time, location, late_threshold_minutes, status FROM sessions WHERE session_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Convert SQL types to Java time types
                    java.sql.Date sqlDate = rs.getDate("session_date");
                    java.sql.Time sqlStartTime = rs.getTime("start_time");
                    java.sql.Time sqlEndTime = rs.getTime("end_time");
                    return new Session(
                            rs.getInt("session_id"),
                            rs.getString("course_name"),
                            sqlDate != null ? sqlDate.toLocalDate() : null,
                            sqlStartTime != null ? sqlStartTime.toLocalTime() : null,
                            sqlEndTime != null ? sqlEndTime.toLocalTime() : null,
                            rs.getString("location"),
                            rs.getInt("late_threshold_minutes"),
                            rs.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // insert 
    public void save(Session s) {
        String sql = "INSERT INTO sessions (course_name, session_date, start_time, end_time, location, late_threshold_minutes, status) VALUES (?, ?, ?, ?, ?, ?, ?) ";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Convert LocalDate to java.sql.Date
            java.sql.Date sqlDate = java.sql.Date.valueOf(s.getSessionDate());
            
            // Convert LocalTime to java.sql.Time
            java.sql.Time sqlStartTime = java.sql.Time.valueOf(s.getStartTime());
            java.sql.Time sqlEndTime = java.sql.Time.valueOf(s.getEndTime());

            ps.setString(1, s.getCourse());
            ps.setDate(2, sqlDate);
            ps.setTime(3, sqlStartTime);
            ps.setTime(4, sqlEndTime);
            ps.setString(5, s.getLocation());
            ps.setInt(6, s.getLateThresholdMinutes());
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

    // delete by id
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

    // delete all
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

    // update status 
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
