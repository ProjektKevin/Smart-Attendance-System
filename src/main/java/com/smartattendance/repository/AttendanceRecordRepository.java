package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.controller.student.AttendanceRow;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;
import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * Repository class for performing CRUD operations on the attendance table.
 * 
 * This class provides data access methods for retrieving, saving, updating,
 * and deleting {@link AttendanceRecord} objects from the database. 
 * 
 * @author Chue Wan Yan (codes + javadoc comments)
 * @author Lim Jia Hui (codes)
 * 
 * @version 21:36 12 Nov 2025
 */
public class AttendanceRecordRepository {

    private final StudentRepository studentRepo; // Reference to studentRepository
    private final SessionRepository sessionRepo; // Reference to SessionRepository
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance(); // App logger to show message


    /**
     * Creates a new repository with initialized student and session repositories.
     */
    public AttendanceRecordRepository() {
        this.studentRepo = new StudentRepository();
        this.sessionRepo = new SessionRepository();
    }

     /**
     * Capitalizes a string by converting it to lower case and then upper-casing
     * the first character.
     *
     * @param str the input string
     * @return a formatted, capitalized string
     */
    public String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        str = str.toLowerCase();
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Converts a SQL {@link Timestamp} to a {@link LocalDateTime}, returning null
     * if the timestamp is null.
     *
     * @param ts the SQL timestamp
     * @return the matching LocalDateTime or null
     */
    private LocalDateTime toLocalDateTime(Timestamp ts) {
        // small helper to avoid NPE on nullable timestamps
        return ts != null ? ts.toLocalDateTime() : null;
    }

    /**
     * Retrieves all attendance records from the database.
     *
     * @return a list of all {@link AttendanceRecord} entries
     */
    public List<AttendanceRecord> findAll() {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT user_id, session_id, note, confidence, marked_at, "
                     + "last_seen, method, status FROM attendance";

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student student = studentRepo.findById(rs.getInt("user_id"));
                Session session = sessionRepo.findById(rs.getInt("session_id"));

                AttendanceStatus status 
                        = AttendanceStatus.valueOf(rs.getString("status").toUpperCase());
                MarkMethod method = MarkMethod.valueOf(rs.getString("method").toUpperCase());

                AttendanceRecord record = new AttendanceRecord(
                        student,
                        session,
                        status,
                        rs.getDouble("confidence"),
                        method,
                        toLocalDateTime(rs.getTimestamp("marked_at")),
                        toLocalDateTime(rs.getTimestamp("last_seen")));

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

    /**
     * Finds all attendance records for a given session ID.
     *
     * @param sessionId the session identifier
     * @return list of attendance records for the session
     */
    public List<AttendanceRecord> findBySessionId(int sessionId) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT user_id, session_id, note, confidence, marked_at, last_seen, "
                     + "method, status FROM attendance WHERE session_id = ? ORDER BY user_id ASC";

        try (Connection conn = DatabaseUtil.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = studentRepo.findById(rs.getInt("user_id"));
                    Session session = sessionRepo.findById(rs.getInt("session_id"));

                    AttendanceStatus status 
                            = AttendanceStatus.valueOf(rs.getString("status").toUpperCase());
                    MarkMethod method = MarkMethod.valueOf(rs.getString("method").toUpperCase());

                    AttendanceRecord record = new AttendanceRecord(
                            student,
                            session,
                            status,
                            rs.getDouble("confidence"),
                            method,
                            toLocalDateTime(rs.getTimestamp("marked_at")),
                            toLocalDateTime(rs.getTimestamp("last_seen")));

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

    /**
     * Retrieves attendance records for a given session which 
     * attendance status is PENDING.
     *
     * @param sessionId the session identifier
     * @param status    the attendance status to filter by
     * @return list of matching attendance records
     */
    public List<AttendanceRecord> findPendingAttendanceBySessionId(
            int sessionId, 
            AttendanceStatus status) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT user_id, session_id, note, confidence, marked_at, last_seen, "
                     + "method, status FROM attendance WHERE session_id = ? AND status = ?";

        try (Connection conn = DatabaseUtil.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            // db seems to store capitalised (Present, Absent, …)
            ps.setString(2, capitalize(status.toString()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = studentRepo.findById(rs.getInt("user_id"));
                    Session session = sessionRepo.findById(rs.getInt("session_id"));

                    MarkMethod method 
                            = MarkMethod.valueOf(rs.getString("method").toUpperCase());

                    AttendanceRecord record = new AttendanceRecord(
                            student,
                            session,
                            status,
                            rs.getDouble("confidence"),
                            method,
                            toLocalDateTime(rs.getTimestamp("marked_at")),
                            toLocalDateTime(rs.getTimestamp("last_seen")));

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

     /**
     * Retrieves a single attendance record based on student Id and session Id.
     *
     * @param studentId the student Id
     * @param sessionId the session Id
     * @return the matching record, or null if not found
     */
    public AttendanceRecord findById(int studentId, int sessionId) {
        String sql = "SELECT user_id, session_id, note, confidence, marked_at, last_seen, "
                     + "method, status FROM attendance WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student student = studentRepo.findById(rs.getInt("user_id"));
                    Session session = sessionRepo.findById(rs.getInt("session_id"));

                    AttendanceStatus status 
                            = AttendanceStatus.valueOf(rs.getString("status").toUpperCase());
                    MarkMethod method = MarkMethod.valueOf(rs.getString("method").toUpperCase());

                    AttendanceRecord record = new AttendanceRecord(
                            student,
                            session,
                            status,
                            rs.getDouble("confidence"),
                            method,
                            toLocalDateTime(rs.getTimestamp("marked_at")),
                            toLocalDateTime(rs.getTimestamp("last_seen")));

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

    /**
     * Retrieves attendance history for a specific student, formatted for
     * display in the student dashboard.
     *
     * @param studentId the student Id
     * @return a list of attendance summary rows
     */
    public List<AttendanceRow> findByStudentId(int studentId) {
        List<AttendanceRow> result = new ArrayList<>();
        String sql = "SELECT a.user_id, a.session_id, "
                + "       s.session_date, s.start_time, s.end_time, "
                + "       c.course_code, c.course_name, "
                + "       a.status "
                + "FROM attendance a "
                + "JOIN sessions s ON a.session_id = s.session_id "
                + "JOIN courses c ON s.course_id = c.course_id "
                + "WHERE a.user_id = ? "
                + "ORDER BY s.session_date DESC, s.start_time DESC";

        try (Connection conn = DatabaseUtil.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

                while (rs.next()) {
                    LocalDate sessionDate = rs.getDate("session_date").toLocalDate();

                    Timestamp startTs = rs.getTimestamp("start_time");
                    Timestamp endTs = rs.getTimestamp("end_time");

                    String startTime = startTs != null
                            ? startTs.toLocalDateTime().format(timeFmt)
                            : "";
                    String endTime = endTs != null
                            ? endTs.toLocalDateTime().format(timeFmt)
                            : "";

                    // course column in AttendanceRow → we put "CODE - NAME"
                    String courseCol = rs.getString("course_code") + " - " 
                                       + rs.getString("course_name");

                    // session column in AttendanceRow → we put "HH:mm - HH:mm"
                    String sessionCol = startTime + (endTime.isEmpty() ? "" : " - " + endTime);

                    String status = rs.getString("status");

                    result.add(new AttendanceRow(sessionDate, courseCol, sessionCol, status));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Saves a new attendance record to the database.
     *
     * @param record the attendance record to save
     * @return true if the record was inserted, false otherwise
     */
    public boolean save(AttendanceRecord record) {
        String sql = "INSERT INTO attendance (user_id, session_id, note, confidence, marked_at, "
                     + "last_seen, method, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, record.getStudent().getStudentId());
            ps.setInt(2, record.getSession().getSessionId());
            ps.setString(3, record.getNote());
            ps.setDouble(4, record.getConfidence());
            ps.setTimestamp(5, Timestamp.valueOf(record.getTimestamp()));
            ps.setTimestamp(6, Timestamp.valueOf(record.getLastSeen()));
            ps.setString(7, capitalize(record.getMethod().toString()));
            ps.setString(8, capitalize(record.getStatus().toString()));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing attendance record.
     *
     * @param record the record containing updated values
     * @return true if the update succeeded
     */
    public boolean update(AttendanceRecord record) {
        String sql = "UPDATE attendance SET status = ?, method = ?, confidence = ?, marked_at = ?, " 
                     + "last_seen = ?, note = ? WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, capitalize(record.getStatus().toString()));
            ps.setString(2, capitalize(record.getMethod().toString()));
            ps.setDouble(3, record.getConfidence());
            ps.setTimestamp(4, Timestamp.valueOf(record.getTimestamp()));
            ps.setTimestamp(5, Timestamp.valueOf(record.getLastSeen()));
            ps.setString(6, record.getNote());
            ps.setInt(7, record.getStudent().getStudentId());
            ps.setInt(8, record.getSession().getSessionId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates only the last_seen column for auto-mark attendance flows.
     *
     * @param record the record whose lastSeen timestamp should be updated
     * @return true if the update succeeded
     */
    public boolean updateLastSeen(AttendanceRecord record) {
        String sql = "UPDATE attendance SET last_seen = ? WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            LocalDateTime lastSeen = record.getLastSeen() != null
                    ? record.getLastSeen()
                    : LocalDateTime.now();

            ps.setTimestamp(1, Timestamp.valueOf(lastSeen));
            ps.setInt(2, record.getStudent().getStudentId());
            ps.setInt(3, record.getSession().getSessionId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // public StudentRepository getStudentRepo() {
    //     return studentRepo;
    // }

    // public SessionRepository getSessionRepo() {
    //     return sessionRepo;
    // }

    /**
     * Updates the attendance status (manual marking only).
     *
     * @param record the attendance record containing the new status
     */
    public void updateStatus(AttendanceRecord record) {
        String sql = "UPDATE attendance SET marked_at = ?, last_seen = ?, method = 'Manual', " 
                     + "status = ? WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(record.getTimestamp()));
            ps.setTimestamp(2, Timestamp.valueOf(record.getLastSeen()));
            ps.setString(3, capitalize(record.getStatus().toString()));
            ps.setInt(4, record.getStudent().getStudentId());
            ps.setInt(5, record.getSession().getSessionId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the note field for a student's attendance record.
     *
     * @param record the record containing the updated note
     */
    public void updateNote(AttendanceRecord record) {
        String sql = "UPDATE attendance SET note = ? WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, record.getNote());
            ps.setInt(2, record.getStudent().getStudentId());
            ps.setInt(3, record.getSession().getSessionId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes an attendance record for a given student and session.
     *
     * @param record the attendance record to delete
     */
    public void deleteRecord(AttendanceRecord record) {
        String sql = "DELETE FROM attendance WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, record.getStudent().getStudentId());
            ps.setInt(2, record.getSession().getSessionId());
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                appLogger.info("Record with StudentId: " + record.getStudent().getStudentId() 
                        + " deleted successfully");
            } else {
                appLogger.info("No record found with StudentId: " + record.getStudent().getStudentId()
                        + " SessionId: " + record.getSession().getSessionId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
