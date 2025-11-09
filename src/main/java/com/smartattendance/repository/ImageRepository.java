package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.entity.Image;

/**
 * Repository for image and face data persistence to Supabase
 * Handles all database operations for student_image and face_data tables
 */
public class ImageRepository {

    /**
     * PHASE 1: Insert image metadata to student_image table
     * This is called during capture phase
     *
     * @param studentId the student ID
     * @param imagePath the file path of the saved image
     * @return the generated image_id, or -1 if insert failed
     */
    public int insertStudentImage(int studentId, String imagePath) {
        String sql = "INSERT INTO student_image (student_id, image_url, added_at) VALUES (?, ?, ?) RETURNING image_id;";

        // Conversion from java local date time to timestamp in sql
        java.sql.Timestamp sqlCurrentTime = java.sql.Timestamp.valueOf(LocalDateTime.now());

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setString(2, imagePath);
            ps.setTimestamp(3, sqlCurrentTime);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("image_id");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error inserting student image: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * PHASE 2: Insert face data (histogram) for a student
     * This is called during background processing phase after histogram is computed
     * Uses byte array format for binary histogram storage
     *
     * @param studentId      the student ID
     * @param histogramBytes the histogram as byte array (binary format)
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
     * Retrieve all images for a specific student
     *
     * @param studentId the student ID
     * @return list of Image objects for the student
     */
    public List<Image> findImagesByStudentId(int studentId) {
        List<Image> images = new ArrayList<>();
        String sql = "SELECT image_id, student_id, image_url FROM student_image WHERE student_id = ? ORDER BY added_at DESC;";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    images.add(new Image(
                            rs.getInt("image_id"),
                            String.valueOf(rs.getInt("student_id")),
                            rs.getString("image_url")));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving images for student " + studentId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return images;
    }

    /**
     * Retrieve a single image by its ID
     *
     * @param imageId the image ID
     * @return Image object if found, null otherwise
     */
    public Image findImageById(int imageId) {
        String sql = "SELECT image_id, student_id, image_url FROM student_image WHERE image_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, imageId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Image(
                            rs.getInt("image_id"),
                            String.valueOf(rs.getInt("student_id")),
                            rs.getString("image_url"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving image " + imageId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get the count of images for a student
     *
     * @param studentId the student ID
     * @return number of images enrolled
     */
    public int countImagesByStudentId(int studentId) {
        String sql = "SELECT COUNT(*) as count FROM student_image WHERE student_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error counting images for student " + studentId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Retrieve histogram bytes for a student from face_data table
     * Used for face recognition - loads the trained average histogram
     *
     * @param studentId the student ID
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
