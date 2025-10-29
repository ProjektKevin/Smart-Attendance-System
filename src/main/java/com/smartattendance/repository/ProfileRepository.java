package com.smartattendance.repository;

import com.smartattendance.model.Profile;
import com.smartattendance.util.DatabaseUtil;

import java.sql.*;

public class ProfileRepository {

    public boolean createProfile(String firstName, String lastName, String phoneNo, Integer userId) {
        String sql = "INSERT INTO profile(first_name, last_name, phone_no, user_id) VALUES(?,?,?,?)";
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
                        rs.getString("phone_no"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateProfileById(String firstName, String lastName, String phoneNo, Integer userId) {
        String sql = "UPDATE profile SET first_name = ?, last_name=?, phone_no=? WHERE user_id = ?";
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
