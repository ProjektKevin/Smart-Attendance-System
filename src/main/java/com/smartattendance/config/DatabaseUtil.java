package com.smartattendance.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    private static final String URL = ENV.getDatabaseURL();
    private static final String USER = ENV.getDatabaseUser();
    private static final String PASSWORD = ENV.getDatabasePassword();

    public static Connection getConnection() {
        try {
            if (URL == null || USER == null || PASSWORD == null) {
                throw new SQLException("Missing Database Credentials.");
            }
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
            return null; // Return null on failure
        }
    }
}