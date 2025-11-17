package com.smartattendance.repository;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.entity.Profile;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Profile Repository
 * Performs DB operations for all profile operation
 * Display, Registration, Update, Delete (Not recommended) but works
 * 
 * @author Thiha Swan Htet
 */
public class ProfileRepository {

    /**
     * Creates profile
     * Used for user registration
     *
     * @param firstName The first name of the user
     * @param lastName  The last name of the user
     * @param phoneNo   The phone number of the user
     * @param userId    The user id of the user
     *
     * @return boolean: true if created, else -> false
     */
    public boolean createProfile(String firstName, String lastName, String phoneNo, Integer userId) {
        String sql = "INSERT INTO profile(first_name, last_name, phone_number, user_id) VALUES(?,?,?,?)";
        int rowsAffected = 0;
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, phoneNo);
            pstmt.setInt(4, userId);
            rowsAffected = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowsAffected == 1;
    }

    /**
     * Loads profile
     * Used for displaying profile information
     *
     * @param userId The user id of the user
     *
     * @return Profile object
     */
    public Profile getProfileById(Integer userId) {
        String sql = "SELECT * FROM profile WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Profile(
                        rs.getInt("profile_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone_number"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates profile
     * Used for updating user profile
     *
     * @param firstName The first name of the user
     * @param lastName  The last name of the user
     * @param phoneNo   The phone number of the user
     * @param userId    The user id of the user
     *
     * @return boolean: true if created, else -> false
     */
    public boolean updateProfileById(String firstName, String lastName, String phoneNo, Integer userId) {
        String sql = "UPDATE profile SET first_name = ?, last_name=?, phone_number=?, updated_at = ? WHERE user_id = ?";
        int rowsAffected = 0;
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, phoneNo);
            pstmt.setObject(4, LocalDateTime.now());
            pstmt.setInt(5, userId);

            rowsAffected = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowsAffected == 1;
    }

    /**
     * Deletes profile
     * Used for user profile deletion
     * 
     * @param userId The user id of the user
     *
     * @return boolean: true if created, else -> false
     */
    public boolean deleteProfileById(Integer userId) {
        String sql = "DELETE FROM profile WHERE user_id = ?";
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
