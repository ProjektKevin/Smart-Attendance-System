package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.opencv.core.Mat;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.util.OpenCVUtils;
import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * Repository for face data persistence to Supabase
 * Handles all database operations for face_data table
 * 
 * @author Thiha Swan Htet
 */
public class ImageRepository {
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

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
            appLogger.error("Error retrieving histogram for student " + studentId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Insert face data with both histogram (byte array) and embedding (pgvector)
     * Called after computing average histogram and embedding
     *
     * @param studentId           The student ID
     * @param histogramBytes      The histogram as byte array (binary format)
     * @param averageEmbeddingMat The 128-dimensional embedding as OpenCV Mat
     *
     * @return true if insert was successful, false otherwise
     */
    public boolean insertFaceData(int studentId, byte[] histogramBytes, String averageEmbeddingStringh) {
        String sql = "INSERT INTO face_data (student_id, avg_histogram, avg_embedding, created_at) VALUES (?, ?, ?::vector, ?);";

        java.sql.Timestamp sqlCurrentTime = java.sql.Timestamp.valueOf(LocalDateTime.now());

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setBytes(2, histogramBytes);
            ps.setString(3, averageEmbeddingStringh);
            ps.setTimestamp(4, sqlCurrentTime);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            appLogger.error("Error inserting face data with embedding: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Retrieve embedding for a student from face_data table
     * Returns the 128-dimensional embedding as OpenCV Mat
     *
     * @param studentId The student ID
     *
     * @return embedding as OpenCV Mat, or empty Mat if not found
     */
    public Mat getEmbeddingByStudentId(int studentId) {
        String sql = "SELECT avg_embedding FROM face_data WHERE student_id = ? LIMIT 1;";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String pgvectorString = rs.getString("avg_embedding");
                    if (pgvectorString != null && !pgvectorString.isEmpty()) {
                        return OpenCVUtils.postgresVectorToMat(pgvectorString);
                    }
                }
            }

        } catch (SQLException e) {
            appLogger.error("Error retrieving embedding for student " + studentId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return new Mat(); // Return empty Mat if not found
    }

    /**
     * Retrieve embedding as pgvector string for a student
     * Useful if you need the raw string format for database operations
     *
     * @param studentId The student ID
     *
     * @return embedding as pgvector string "[v1,v2,...,v128]", or null if not found
     */
    public String getEmbeddingStringByStudentId(int studentId) {
        String sql = "SELECT avg_embedding FROM face_data WHERE student_id = ? LIMIT 1;";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("avg_embedding");
                }
            }

        } catch (SQLException e) {
            appLogger.error("Error retrieving embedding string for student " + studentId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update only the embedding for an existing student
     *
     * @param studentId           The student ID
     * @param averageEmbeddingMat The 128-dimensional embedding as OpenCV Mat
     *
     * @return true if update was successful, false otherwise
     */
    public boolean updateEmbeddingByStudentId(int studentId, Mat averageEmbeddingMat) {
        String sql = "UPDATE face_data SET avg_embedding = ?::vector WHERE student_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String pgvectorString = OpenCVUtils.matToPostgresVector(averageEmbeddingMat);

            ps.setString(1, pgvectorString);
            ps.setInt(2, studentId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            appLogger.error("Error updating embedding for student " + studentId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}