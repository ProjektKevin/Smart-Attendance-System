package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.entity.User;

public class PostgresUserRepository {

    public User findUserById(Integer id) {
        String sql = "SELECT user_id, username, email, role, is_email_verified FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getBoolean("is_email_verified"));

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> findUsersByRole(String role) {
        List<User> filteredUsersByRole = new ArrayList<>();
        String sql = "SELECT user_id, username, email, role, is_email_verified FROM users WHERE role = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filteredUsersByRole.add(new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getBoolean("is_email_verified")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filteredUsersByRole;
    }
}