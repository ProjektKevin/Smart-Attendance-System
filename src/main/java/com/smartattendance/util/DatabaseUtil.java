package com.smartattendance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    private static final String URL = "jdbc:postgresql://ep-dry-night-a1qhrjmz-pooler.ap-southeast-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_nrDHTLgGz5o1&sslmode=require&channelBinding=require";
    private static final String USER = "neondb_owner";
    private static final String PASSWORD = "npg_nrDHTLgGz5o1";

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