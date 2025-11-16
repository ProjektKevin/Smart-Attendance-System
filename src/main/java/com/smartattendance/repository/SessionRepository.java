package com.smartattendance.repository;

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
import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * Data Access Object (DAO) for managing Session entities in the database.
 * 
 * This repository handles all CRUD (Create, Read, Update, Delete) operations
 * for Session objects, including complex queries for session status management
 * and automatic session processing.
 * 
 * Key responsibilities:
 * - Session persistence and retrieval
 * - Session status management
 * - Automatic session configuration handling
 * - Database connection management
 * - Data type conversion between Java and SQL
 * 
 * @author Lim Jia Hui
 * @version 18:41 16 Nov 2025
 */
public class SessionRepository {

    // ========== DEPENDENCIES ==========

    /**
     * Application Logger to show on terminal and write file to refer back to
     */
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    // ========== CRUD OPERATIONS ========

    /**
     * Retrieves all sessions from the database with their associated course code.
     * 
     * This method executes a JOIN query between sessions and courses tables
     * to provide complete session information including course codes.
     *
     * @return a list of all Session objects in the database, never null
     * @throws RuntimeException if a database access error occurs
     */
    public List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();
        /** SQL query for retrieving all sessions and their course code */
        String sql = "SELECT s.session_id, c.course_code, s.late_threshold, s.location, s.start_time, s.end_time, s.session_date, s.status, s.auto_start, s.auto_stop FROM sessions s JOIN courses c ON s.course_id = c.course_id;";

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
                        rs.getString("status"),
                        rs.getBoolean("auto_start"),
                        rs.getBoolean("auto_stop")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sessions;
    }

    /**
     * Retrieves a specific session by its unique identifier.
     *
     * @param id the session_id to search for
     * @return the Session object if found, null if no session exists with the given
     *         session_id
     * @throws IllegalArgumentException if the session_id is not positive
     * @throws RuntimeException         if a database access error occurs
     */
    public Session findById(int id) {
        /** SQL query for finding a session by session_id */
        String sql = "SELECT s.session_id, c.course_code, s.late_threshold, s.location, s.start_time, s.end_time, s.session_date, s.status, s.auto_start, s.auto_stop FROM sessions s JOIN courses c ON s.course_id = c.course_id WHERE session_id = ?";
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
                            rs.getString("status"),
                            rs.getBoolean("auto_start"),
                            rs.getBoolean("auto_stop"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks if there are any sessions currently marked as "Open" in the system.
     * The system should only allow one open session at a time.
     *
     * @return true if there is at least one open session, false otherwise
     * @throws RuntimeException if a database access error occurs
     */
    public boolean isSessionOpen() {
        /** SQL query for checking if any sessions are currently open */
        String sql = "SELECT COUNT(*) FROM sessions WHERE status = 'Open';";
        int openSessionsCount = 0;

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                openSessionsCount = rs.getInt(1);
            }

            // Return true if there are open sessions (count > 0), false if there are no
            // open sessions
            return openSessionsCount > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves a new session to the database and updates the session object with the
     * generated session_id.
     * 
     * This method handles the conversion of Java time objects to SQL types
     * and retrieves the auto-generated session_id to update the provided Session
     * object.
     *
     * @param session the Session object to persist, must not be null
     * @throws IllegalArgumentException if the session is null or has invalid data
     * @throws RuntimeException         if a database access error occurs
     */
    public void save(Session s) {
        /** SQL query for inserting a new session */
        String sql = "INSERT INTO sessions (course_id, late_threshold, location, start_time, end_time, session_date, status) VALUES ((SELECT course_id FROM courses WHERE course_code = ?), ?, ?, ?, ?, ?, ?) ";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Convert LocalDate to java.sql.Date for session_date
            java.sql.Date sqlDate = java.sql.Date.valueOf(s.getSessionDate());

            // For timestamp columns, need to combine session_date with the time
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

    /**
     * Deletes a session from the database by its unique identifier.
     *
     * @param id the session_id of the session to delete
     * @return true if the session was deleted, false if no session was found with
     *         the given session_id
     * @throws IllegalArgumentException if the session_id is not positive
     * @throws RuntimeException         if a database access error occurs
     */
    public void deleteById(int id) {
        /** SQL query for deleting a session by seesion_id */
        String sql = "DELETE FROM sessions where session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                appLogger.info("Session deleted successfully");
            } else {
                appLogger.info("No session found with ID: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes all sessions from the database and resets the auto-increment counter.
     * 
     * Warning: This operation is destructive and cannot be undone.
     * Use with extreme caution in production environments.
     *
     * @return the number of sessions deleted
     * @throws RuntimeException if a database access error occurs
     */
    public void deleteAll() {
        /** SQL query for deleting all sessions */
        String deleteSql = "DELETE FROM sessions";
        /** SQL query for resetting session ID sequence */
        String resetSql = "ALTER SEQUENCE sessions_session_id_seq RESTART WITH 1";

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Delete all sessions
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                int deletedCount = ps.executeUpdate();
                appLogger.info("Deleted " + deletedCount + " sessions");
            }

            // Reset auto-increment counter
            try (PreparedStatement ps = conn.prepareStatement(resetSql)) {
                ps.executeUpdate();
                appLogger.info("Reset auto-increment counter");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== SESSION STATUS MANAGEMENT ==========

    /**
     * Updates the status of a specific session.
     *
     * @param id     the session_id to update
     * @param status the new status to set ("Pending", "Open", or "Closed")
     * @throws IllegalArgumentException if the session_id is not positive or status
     *                                  is invalid
     * @throws RuntimeException         if a database access error occurs
     */
    public void updateStatus(int id, String status) {
        /** SQL query for updating session status */
        String sql = "UPDATE sessions SET status = ? WHERE session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== AUTO-SESSION MANAGEMENT ==========

    /**
     * Checks if there are other sessions with auto-start enabled, excluding a
     * specific session.
     * Used to ensure only one session can have auto-start enabled at a time.
     *
     * @param excludeSessionId the session_id to exclude from the check
     * @return true if there are other sessions with auto-start enabled, false
     *         otherwise
     * @throws IllegalArgumentException if the excludeSessionId is not positive
     * @throws RuntimeException         if a database access error occurs
     */
    public boolean hasOtherAutoStartSession(int excludeSessionId) {
        /** SQL query for checking other auto-start sessions */
        String sql = "SELECT COUNT(*) FROM sessions WHERE auto_start = TRUE AND session_id != ?;";
        int sessionsCount = 0;

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, excludeSessionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                sessionsCount = rs.getInt(1);
            }

            // appLogger.info("SessionRepository: Found " + sessionsCount + " other
            // auto-start sessions excluding session " + excludeSessionId);
            return sessionsCount > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if there are other sessions with auto-stop enabled, excluding a
     * specific session.
     * Used to ensure only one session can have auto-stop enabled at a time.
     *
     * @param excludeSessionId the session_id to exclude from the check
     * @return true if there are other sessions with auto-stop enabled, false
     *         otherwise
     * @throws IllegalArgumentException if the excludeSessionId is not positive
     * @throws RuntimeException         if a database access error occurs
     */
    public boolean hasOtherAutoStopSession(int excludeSessionId) {
        /** SQL query for checking other auto-stop sessions */
        String sql = "SELECT COUNT(*) FROM sessions WHERE auto_stop = TRUE AND session_id != ?;";
        int sessionsCount = 0;

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, excludeSessionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                sessionsCount = rs.getInt(1);
            }

            // appLogger.info("SessionRepository: Found " + sessionsCount + " other
            // auto-stop sessions excluding session " + excludeSessionId);
            return sessionsCount > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the automatic start and stop settings for a specific session.
     *
     * @param sessionId the session_id to update
     * @param autoStart whether automatic start should be enabled
     * @param autoStop  whether automatic stop should be enabled
     * @throws IllegalArgumentException if the session_id is not positive
     * @throws RuntimeException         if a database access error occurs
     */
    public void updateAutoSettings(int sessionId, boolean autoStart, boolean autoStop) {
        /** SQL query for updating auto-settings */
        String sql = "UPDATE sessions SET auto_start = ?, auto_stop = ? WHERE session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, autoStart);
            ps.setBoolean(2, autoStop);
            ps.setInt(3, sessionId);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}