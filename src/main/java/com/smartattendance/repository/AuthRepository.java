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
                            Role.valueOf(rs.getString("role").toUpperCase()),
                            rs.getBoolean("is_email_verified"));

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean registerEmailRole(String email, String role) {
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
}
