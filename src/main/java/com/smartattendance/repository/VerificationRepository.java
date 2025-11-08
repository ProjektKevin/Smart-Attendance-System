package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

import com.smartattendance.model.entity.Verification;
import com.smartattendance.model.enums.AuthVerification;
import com.smartattendance.config.DatabaseUtil;

public class VerificationRepository {

    /**
     * Create a new verification token in the database
     *
     * @param req The verification entity
     * @return The created verification with generated ID, or null if creation
     *         failed
     */
    public Verification createVerification(Verification req) {
        String sql = "INSERT INTO verification(identifier, token, expires_at, user_id) VALUES(?,?,?,?)";

        // Conversion from java local date time to timestamp in sql
        java.sql.Timestamp sqlExpTime = java.sql.Timestamp.valueOf(req.getExpTime());

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, req.getIdentifier().name());
            pstmt.setString(2, req.getToken());
            pstmt.setTimestamp(3, sqlExpTime);
            pstmt.setInt(4, req.getUserId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return new Verification(
                            generatedKeys.getInt(1),
                            req.getIdentifier(),
                            req.getToken(),
                            req.getExpTime(),
                            req.getUserId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check if a token is valid and not expired for a specific user
     *
     * @param userId The user ID
     * @param token  The token to verify
     * @param type   The verification type (FORGOT_PASSWORD or VERIFICATION)
     * @return true if token exists, matches, and is not expired
     */
    public boolean isTokenValid(Integer userId, String token, AuthVerification type) {
        String sql = "SELECT expires_at FROM verification WHERE user_id = ? AND token = ? AND identifier = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, token);
            pstmt.setString(3, type.name());

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                LocalDateTime expTime = rs.getTimestamp("expires_at").toLocalDateTime();
                // Check token expiration
                return LocalDateTime.now().isBefore(expTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete a specific verification record by user ID and verification type
     *
     * @param userId The user ID
     * @param type   The verification type
     * @return true if deletion was successful
     */
    public boolean deleteVerification(Integer userId, AuthVerification type) {
        String sql = "DELETE FROM verification WHERE user_id = ? AND identifier = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, type.name());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete all expired verification tokens from the database
     *
     * @return true if deletion was successful
     */
    public boolean deleteExpiredVerifications() {
        String sql = "DELETE FROM verification WHERE expires_at < NOW()";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get a verification record by user ID and type
     *
     * @param userId The user ID
     * @param type   The verification type
     * @return The verification entity, or null if not found
     */
    public Verification getVerification(Integer userId, AuthVerification type) {
        String sql = "SELECT verification_id, identifier, token, expires_at, user_id FROM verification WHERE user_id = ? AND identifier = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, type.name());

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Verification(
                        rs.getInt("verification_id"),
                        AuthVerification.valueOf(rs.getString("name")),
                        rs.getString("token"),
                        rs.getTimestamp("expires_at").toLocalDateTime(),
                        rs.getInt("user_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
