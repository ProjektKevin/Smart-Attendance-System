package com.smartattendance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    private static final String URL = "jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?user=postgres.muholwznrkonrcjesdij";
    private static final String USER = "postgres.muholwznrkonrcjesdij";
    private static final String PASSWORD = "TheDawn5";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
            return null; // Return null on failure
        }
    }
}