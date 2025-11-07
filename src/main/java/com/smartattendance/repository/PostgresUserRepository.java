package com.smartattendance.repository;

import com.smartattendance.model.enums.Role;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.entity.User;

import java.util.List;
import java.util.ArrayList;

import java.sql.*;

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
                            Role.valueOf(rs.getString("role").toUpperCase()),
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
                            Role.valueOf(rs.getString("role").toUpperCase()),
                            rs.getBoolean("is_email_verified")));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filteredUsersByRole;
    }

    public boolean deleteUserById(Integer userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        int rowsAffected = 0;
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            rowsAffected = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowsAffected == 1;
    }
}