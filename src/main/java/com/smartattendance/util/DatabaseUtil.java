package com.smartattendance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    private static final String URL = System.getenv("DATABASE_URL");
    private static final String USER = System.getenv("DATABASE_USER");
    private static final String PASSWORD = System.getenv("DATABASE_PASSWORD");

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