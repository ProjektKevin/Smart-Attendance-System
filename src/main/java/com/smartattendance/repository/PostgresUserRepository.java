package com.smartattendance.repository;

import com.smartattendance.model.User;
import com.smartattendance.util.DatabaseUtil;

import java.sql.*;

public class PostgresUserRepository {

    public User findByUsername(String username) {
        String sql = "SELECT user_id, is_email_verified, password_hash, role FROM users WHERE username = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("user_id"),
                            rs.getBoolean("is_email_verified"),
                            rs.getString("password_hash"),
                            rs.getString("role"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}