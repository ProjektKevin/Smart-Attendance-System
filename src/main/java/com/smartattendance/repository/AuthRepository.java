package com.smartattendance.repository;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.entity.User;

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
                            rs.getString("role"),
                            rs.getBoolean("is_email_verified"));

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
