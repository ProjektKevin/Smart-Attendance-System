package com.smartattendance.repository;

import com.smartattendance.model.Profile;
import com.smartattendance.util.DatabaseUtil;

import java.sql.*;

public class ProfileRepository {
    public Profile getProfileById(String userId) {
        String sql = "SELECT * FROM profile WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Profile(
                        rs.getString("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone_no"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateProfileById(String firstName, String lastName, String phoneNo, String userId) {
        String sql = "UPDATE profile SET first_name = ?, last_name=?, phone_no=? WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, phoneNo);
            pstmt.setString(4, userId);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
