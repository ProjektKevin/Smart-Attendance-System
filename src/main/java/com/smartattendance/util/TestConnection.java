package com.smartattendance.util;

import java.sql.Connection;
import java.sql.SQLException;

import com.smartattendance.config.DatabaseUtil;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            System.out.println("Connected to Supabase successfully!");
        } catch (SQLException e) {
            System.out.println("Connection failed:");
            e.printStackTrace();
        }
    }
}