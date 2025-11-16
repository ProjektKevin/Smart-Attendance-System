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

public class SessionRepository {

    /**
     * Application Logger to show on terminal and write file to refer back to
     */
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    // Select all sessions
    public List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();
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

    // Select session by id
    public Session findById(int id) {
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

    // Check if there is already a session opened (should be either 1 or 0)
    public boolean isSessionOpen() {
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

    // Delete all sessions and reset id to start from 1
    public void deleteAll() {
        String deleteSql = "DELETE FROM sessions";
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

    // Update status of session by id
    public void updateStatus(int id, String status) {
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

    // Check if there is already a session with auto_start (either 0 or 1)
    public boolean hasAutoStartSession() {
        String sql = "SELECT COUNT(*) FROM sessions WHERE auto_start = TRUE;";
        int sessionsCount = 0;

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                sessionsCount = rs.getInt(1);
            }

            // Return true if there are auto_start sessions (count > 0), false if there are
            // none
            return sessionsCount > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if there are other sessions with auto_start = TRUE excluding the given
    // session
    public boolean hasOtherAutoStartSession(int excludeSessionId) {
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

    // Check if there is already a session with auto_stop (either 0 or 1)
    public boolean hasAutoStopSession() {
        String sql = "SELECT COUNT(*) FROM sessions WHERE auto_stop = TRUE;";
        int sessionsCount = 0;

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                sessionsCount = rs.getInt(1);
            }

            // Return true if there are auto_stop sessions (count > 0), false if there are
            // none
            return sessionsCount > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if there are other sessions with auto_start = TRUE excluding the given
    // session
    public boolean hasOtherAutoStopSession(int excludeSessionId) {
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

    // Update settings of auto_start and auto_stop
    public void updateAutoSettings(int sessionId, boolean autoStart, boolean autoStop) {
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