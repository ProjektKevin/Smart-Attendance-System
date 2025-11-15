package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.smartattendance.model.enums.Role;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.entity.User;

/**
 * User Repository
 * Performs DB operations for all users in general
 * Finding user, Deletion, and retriving role-based user
 * 
 * @author Thiha Swan Htet
 */
public class UserRepository {

    /**
     * Retrieve User by the id
     * Used for displaying basic user info
     *
     * @param id The id of the user
     *
     * @return User object
     */
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

    /**
     * Retrieve User by the role
     * Used for table display under admin
     * Reusable if an app expands to allow filter between different roles: Student,
     * Manager, Instructor...etc
     * 
     * @param role The role needed to be filtered
     *
     * @return List<User> List of all User object
     */
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

    /**
     * Delete the user
     * Used for admin to delete student
     *
     * @param id The id of the user
     *
     * @return boolean: true if created, else -> false
     */
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