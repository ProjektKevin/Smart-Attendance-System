package com.smartattendance.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * Utility class for creating JDBC connections to the application database.
 *
 * This class centralizes all logic for obtaining a {@link Connection} object,
 * so that the rest of the application does not need to know the low-level
 * details (JDBC URL, username, password).
 *
 * Credentials are retrieved from the ENV helper, which in turn reads from
 * environment variables or an external configuration source. This matches
 * the project requirement to externalize configuration instead of hard-coding
 * database settings in the source code.
 */
public class DatabaseUtil {

    /**
     * JDBC URL for the database connection.
     * <p>
     * Example (PostgreSQL): jdbc:postgresql://host:port/database
     */
    private static final String URL = ENV.getDatabaseURL();

    /**
     * Database username used for authentication.
     */
    private static final String USER = ENV.getDatabaseUser();

    /**
     * Database password used for authentication.
     */
    private static final String PASSWORD = ENV.getDatabasePassword();

    /**
     * Database password used for authentication.
     */
    private static final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    /**
     * Obtains a new JDBC {@link Connection} to the configured database.
     *
     * This method:
     * <ol>
     * <li>Checks that URL, user, and password are available.</li>
     * <li>Uses {@link DriverManager#getConnection(String, String, String)}
     * to create a connection.</li>
     * <li>Logs and returns {@code null} if any error occurs.</li>
     * </ol>
     *
     * Callers must always check for {@code null} before using the returned
     * connection and handle the failure case appropriately (e.g. show an
     * error dialog or disable database-dependent features).
     *
     * @return a live {@link Connection} if successful, or {@code null} if the
     *         connection attempt fails or credentials are missing.
     */
    public static Connection getConnection() {
        try {
            // Basic validation to avoid confusing DriverManager errors
            if (URL == null || USER == null || PASSWORD == null) {
                throw new SQLException("Missing database credentials.");
            }
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            appLogger.error("Failed to connect to database", e);
            // Returning null signals that the connection could not be established.
            return null;
        }
    }
}