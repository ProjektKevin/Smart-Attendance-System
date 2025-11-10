package com.smartattendance.repository;

// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;
// import java.sql.Timestamp;
// import java.util.ArrayList;
// import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.controller.student.StudentAttendanceController.AttendanceRow;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;

public class AttendanceRecordRepository {

    private StudentRepository studentRepo;
    private SessionRepository sessionRepo;

    public AttendanceRecordRepository() {
        this.studentRepo = new StudentRepository();
        this.sessionRepo = new SessionRepository();
    }

    // helper class to convert first character to upper case for mark method and attendance status
    public String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        str = str.toLowerCase();
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // select all
    public List<AttendanceRecord> findAll() {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT user_id, session_id, note, confidence, marked_at, last_seen, method, status FROM attendance";

        try (Connection conn = DatabaseUtil.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Load the actual Student and Session objects
                Student student = studentRepo.findById(rs.getInt("user_id"));
                Session session = sessionRepo.findById(rs.getInt("session_id"));

                // F_MA: modified by felicia handling marking attendance
                // Convert strings to enums
                AttendanceStatus status = AttendanceStatus.valueOf(rs.getString("status").toUpperCase());
                MarkMethod method = MarkMethod.valueOf(rs.getString("method").toUpperCase());

                AttendanceRecord record = new AttendanceRecord(
                        student,
                        session,
                        status,
                        rs.getDouble("confidence"),
                        method,
                        rs.getTimestamp("marked_at").toLocalDateTime(),
                        // rs.getTimestamp("marked_at").toLocalDateTime(),
                        rs.getTimestamp("last_seen").toLocalDateTime()
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
        String sql = "SELECT user_id, session_id, note, confidence, marked_at, last_seen, method, status FROM attendance WHERE session_id = ? ORDER BY user_id ASC";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            System.out.println("Running query for session_id = " + sessionId); // ##for testing

            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = studentRepo.findById(rs.getInt("user_id"));
                    Session session = sessionRepo.findById(rs.getInt("session_id"));

                    // F_MA: modified by felicia handling marking attendance
                    // Convert strings to enums
                    AttendanceStatus status = AttendanceStatus.valueOf(rs.getString("status").toUpperCase());
                    MarkMethod method = MarkMethod.valueOf(rs.getString("method").toUpperCase());

                    AttendanceRecord record = new AttendanceRecord(
                            student,
                            session,
                            status,
                            rs.getDouble("confidence"),
                            method,
                            rs.getTimestamp("marked_at").toLocalDateTime(),
                            // rs.getTimestamp("marked_at").toLocalDateTime(),
                            rs.getTimestamp("last_seen").toLocalDateTime()
                    );

                    // Set the note if it exists
                    String note = rs.getString("note");
                    if (note != null) {
                        record.setNote(note);
                    }

                    records.add(record);

                    // ##for testing
                    System.out.println(
                            "Found record: user_id=" + rs.getInt("user_id")
                            + ", status=" + rs.getString("status")
                    );

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    // select pending attendance record by session_id - returns multiple records for a session
    public List<AttendanceRecord> findPendingAttendanceBySessionId(int sessionId, AttendanceStatus status) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT user_id, session_id, note, confidence, marked_at, last_seen, method, status FROM attendance WHERE session_id = ? AND status = ?";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            System.out.println("Running query for session_id = " + sessionId); // ##for testing

            ps.setInt(1, sessionId);
            ps.setString(2, capitalize(status.toString()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = studentRepo.findById(rs.getInt("user_id"));
                    Session session = sessionRepo.findById(rs.getInt("session_id"));

                    // F_MA: modified by felicia handling marking attendance
                    // Convert strings to enums
                    // AttendanceStatus status = AttendanceStatus.valueOf(rs.getString("status").toUpperCase());
                    MarkMethod method = MarkMethod.valueOf(rs.getString("method").toUpperCase());

                    AttendanceRecord record = new AttendanceRecord(
                            student,
                            session,
                            status,
                            rs.getDouble("confidence"),
                            method,
                            rs.getTimestamp("marked_at").toLocalDateTime(),
                            // rs.getTimestamp("marked_at").toLocalDateTime(),
                            rs.getTimestamp("last_seen").toLocalDateTime()
                    );

                    // Set the note if it exists
                    String note = rs.getString("note");
                    if (note != null) {
                        record.setNote(note);
                    }

                    records.add(record);

                    // ##for testing
                    System.out.println(
                            "Found record: user_id=" + rs.getInt("user_id")
                            + ", status=" + rs.getString("status")
                    );

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    // If you need to find a single record by composite key (student_id + session_id)
    public AttendanceRecord findById(int studentId, int sessionId) {
        String sql = "SELECT user_id, session_id, note, confidence, marked_at, last_seen, method, status FROM attendance WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student student = studentRepo.findById(rs.getInt("user_id"));
                    Session session = sessionRepo.findById(rs.getInt("session_id"));

                    // F_MA: modified by felicia handling marking attendance
                    // Convert strings to enums
                    AttendanceStatus status = AttendanceStatus.valueOf(rs.getString("status").toUpperCase());
                    MarkMethod method = MarkMethod.valueOf(rs.getString("method").toUpperCase());

                    AttendanceRecord record = new AttendanceRecord(
                            student,
                            session,
                            status,
                            rs.getDouble("confidence"),
                            method,
                            // rs.getTimestamp("marked_at").toLocalDateTime()
                            rs.getTimestamp("marked_at").toLocalDateTime(),
                            rs.getTimestamp("last_seen").toLocalDateTime()
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

    public List<AttendanceRow> findByStudentId(int studentId) {
        List<AttendanceRow> result = new ArrayList<>();
        String sql = "SELECT a.user_id, a.session_id, s.session_date, s.start_time, s.end_time, s.course_id, c.course_name, c.course_code, a.note, a.confidence, a.marked_at, a.last_seen, a.method, a.status FROM attendance a " + 
                     "JOIN sessions s ON a.session_id = s.session_id " + 
                     "JOIN courses c ON s.course_id = c.course_id " + 
                     "WHERE a.user_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            // ps.setInt(2, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate sessionDate = rs.getDate("session_date").toLocalDate();
                    String startTime = rs.getTimestamp("start_time").toLocalDateTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                    String endTime = rs.getTimestamp("end_time").toLocalDateTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    String status = rs.getString("status");

                    result.add(new AttendanceRow(sessionDate, startTime, endTime, courseCode, courseName, status));
                }
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean save(AttendanceRecord record) {
        String sql = "INSERT INTO attendance (user_id, session_id, note, confidence, marked_at, last_seen, method, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

    public boolean update(AttendanceRecord record) {
        String sql = "UPDATE attendance SET status = ?, method = ?, confidence = ?, marked_at = ?, last_seen = ?, note = ? WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

    // F_MA: added by felicia handling marking attendance
    // this one is for update last seen only if the student is already marked present via auto mark
    public boolean updateLastSeen(AttendanceRecord record) {
        String sql = "UPDATE attendance SET last_seen = ? WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            // ps.setString(1, record.getStatus().toString());
            // ps.setString(2, record.getMethod().toString());
            // ps.setDouble(3, record.getConfidence());
            // ps.setTimestamp(4, Timestamp.valueOf(record.getTimestamp()));
            // ps.setString(5, record.getNote());
            ps.setTimestamp(1, Timestamp.valueOf((record.getTimestamp())));
            ps.setInt(2, record.getStudent().getStudentId());
            ps.setInt(3, record.getSession().getSessionId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public StudentRepository getStudentRepo() {
        return studentRepo;
    }

    public SessionRepository getSessionRepo() {
        return sessionRepo;
    }

    public void updateStatus(AttendanceRecord record) {
        String sql = "UPDATE attendance SET marked_at = ?, last_seen = ?, method = 'Manual', status = ? WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(record.getTimestamp()));
            ps.setTimestamp(2, Timestamp.valueOf(record.getLastSeen()));
            ps.setString(3, capitalize(record.getStatus().toString()));
            ps.setInt(4, record.getStudent().getStudentId());
            ps.setInt(5, record.getSession().getSessionId());
            // Set both marked_at and last_seen to current timestamp
            // Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            // ps.setTimestamp(1, currentTimestamp);
            // ps.setTimestamp(2, currentTimestamp);
            // ps.setString(3, record.getStatus());
            // ps.setInt(4, record.getStudent().getStudentId());
            // ps.setInt(5, record.getSession().getSessionId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateNote(AttendanceRecord record) {
        String sql = "UPDATE attendance SET note = ? WHERE user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            // ps.setTimestamp(1, Timestamp.valueOf(record.getTimestamp()));
            // ps.setTimestamp(2, Timestamp.valueOf(record.getLastSeen()));
            ps.setString(1, record.getNote());
            ps.setInt(2, record.getStudent().getStudentId());
            ps.setInt(3, record.getSession().getSessionId());
            // Set both marked_at and last_seen to current timestamp
            // Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            // ps.setTimestamp(1, currentTimestamp);
            // ps.setTimestamp(2, currentTimestamp);
            // ps.setString(3, record.getStatus());
            // ps.setInt(4, record.getStudent().getStudentId());
            // ps.setInt(5, record.getSession().getSessionId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete record by userid and sessionid
    public void deleteRecord(AttendanceRecord record) {
        String sql = "DELETE FROM attendance where user_id = ? AND session_id = ?";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, record.getStudent().getStudentId());
            ps.setInt(2, record.getSession().getSessionId());
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Record with StudentId: " + record.getStudent().getStudentId() + " deleted successfully");
            } else {
                System.out.println("No record found with StudentId: " + record.getStudent().getStudentId() + " SessionId: " + record.getSession().getSessionId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
