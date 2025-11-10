package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.smartattendance.config.DatabaseUtil;

/**
 * Repository for face data persistence to Supabase
 * Handles all database operations for face_data table
 */
public class ImageRepository {

    /**
     * Insert face data (histogram) for a student
     * Called after computing average histogram
     * Uses byte array format for binary histogram storage
     *
     * @param studentId      The student ID
     * @param histogramBytes The histogram as byte array (binary format)
     * 
     * @return true if insert was successful, false otherwise
     */
    public boolean insertFaceData(int studentId, byte[] histogramBytes) {
        String sql = "INSERT INTO face_data (student_id, avg_histogram, created_at) VALUES (?, ?, ?);";

        // Conversion from java local date time to timestamp in sql
        java.sql.Timestamp sqlCurrentTime = java.sql.Timestamp.valueOf(LocalDateTime.now());

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setBytes(2, histogramBytes); // Use setBytes for binary data
            ps.setTimestamp(3, sqlCurrentTime);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting face data: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Retrieve histogram bytes for a student from face_data table
     * Used for face recognition - loads the trained average histogram
     *
     * @param studentId The student ID
     * 
     * @return histogram as byte array, or null if not found
     */
    public byte[] getHistogramByStudentId(int studentId) {
        String sql = "SELECT avg_histogram FROM face_data WHERE student_id = ? LIMIT 1;";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("avg_histogram");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving histogram for student " + studentId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}