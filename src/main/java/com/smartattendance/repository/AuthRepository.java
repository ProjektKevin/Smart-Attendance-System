package com.smartattendance.repository;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.entity.User;
import com.smartattendance.model.enums.Role;

import java.sql.*;

public class AuthRepository {
    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            Role.valueOf(rs.getString("role").toUpperCase()),
                            rs.getBoolean("is_email_verified"));

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            Role.valueOf(rs.getString("role").toUpperCase()),
                            rs.getBoolean("is_email_verified"));

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createEmailRole(String email, String role) {
        String sql = "INSERT INTO users(email, role) VALUES(?,?)";
        int rowsAffected = 0;
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, role);
            rowsAffected = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowsAffected == 1;
    }

    public boolean updatePassword(Integer userId, String hashedPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        int rowsAffected = 0;
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);

            rowsAffected = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowsAffected == 1;
    }

    /**
     * Complete user registration in a transaction
     * Updates users table with username and password
     * Inserts into profile table with firstName and lastName
     *
     * This is a transactional operation - both queries must succeed or both will be
     * rolled back
     *
     * @param userId         The user ID (already exists from admin invitation)
     * @param username       The chosen username
     * @param hashedPassword The hashed password
     * @param firstName      The user's first name
     * @param lastName       The user's last name
     * @return true if both updates succeed, false if any fails
     */
    public boolean createUser(Integer userId, String username, String hashedPassword,
            String firstName, String lastName) {
        // SQL statements
        String updateUserSql = "UPDATE users SET username = ?, password_hash = ?, is_email_verified = ? WHERE user_id = ?";
        String insertProfileSql = "INSERT INTO profile(first_name, last_name, user_id) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Disable auto-commit to start transaction
            conn.setAutoCommit(false);

            try {
                // Step 1: Update users table with username and password
                try (PreparedStatement updateUserStmt = conn.prepareStatement(updateUserSql)) {
                    updateUserStmt.setString(1, username);
                    updateUserStmt.setString(2, hashedPassword);
                    updateUserStmt.setBoolean(3, true);
                    updateUserStmt.setInt(4, userId);

                    int userRowsAffected = updateUserStmt.executeUpdate();
                    if (userRowsAffected != 1) {
                        conn.rollback();
                        System.err.println("Failed to update user record");
                        return false;
                    }
                }

                // Step 2: Insert into profile table with firstName and lastName
                try (PreparedStatement insertProfileStmt = conn.prepareStatement(insertProfileSql)) {
                    insertProfileStmt.setString(1, firstName);
                    insertProfileStmt.setString(2, lastName);
                    insertProfileStmt.setInt(3, userId);

                    int profileRowsAffected = insertProfileStmt.executeUpdate();
                    if (profileRowsAffected != 1) {
                        conn.rollback();
                        System.err.println("Failed to insert profile record");
                        return false;
                    }
                }

                // Both operations succeeded - commit transaction
                conn.commit();
                return true;

            } catch (SQLException e) {
                // Rollback on any error
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                System.err.println("Transaction failed: " + e.getMessage());
                e.printStackTrace();
                return false;

            } finally {
                // Re-enable auto-commit
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

}
