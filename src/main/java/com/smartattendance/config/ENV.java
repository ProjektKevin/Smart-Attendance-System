package com.smartattendance.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * ENV is a small helper class responsible for reading configuration values
 * from a <code>.env</code> file using the dotenv library.
 *
 * This class acts as a single access point for environment-based settings,
 * such as database credentials and SMTP configuration, so that the rest of
 * the application does not need to know how these values are loaded.
 *
 * By centralizing access here, we keep sensitive values (e.g. passwords)
 * out of the source code and externalize them into environment variables,
 * which aligns with good security and configuration practices.
 * 
 * @author Thiha Swan Htet, Ernest Lun
 */
public class ENV {

    /**
     * Shared Dotenv instance used to load key–value pairs from the .env file.
     *
     * Dotenv.load() automatically looks for a .env file in the project root
     * and makes its entries available via dotenv.get("KEY").
     */
    private static final Dotenv dotenv = Dotenv.load();

    /**
     * @return the database JDBC URL from the environment (DATABASE_URL),
     *         or null if not defined.
     */
    public static String getDatabaseURL() {
        return dotenv.get("DATABASE_URL");
    }

    /**
     * @return the database username (DATABASE_USER), or null if not defined.
     */
    public static String getDatabaseUser() {
        return dotenv.get("DATABASE_USER");
    }

    /**
     * @return the database password (DATABASE_PASSWORD), or null if not defined.
     */
    public static String getDatabasePassword() {
        return dotenv.get("DATABASE_PASSWORD");
    }

    /**
     * @return the SMTP host name for outgoing mail (SMTP_HOST), or null if not
     *         defined.
     */
    public static String getSMTPHost() {
        return dotenv.get("SMTP_HOST");
    }

    /**
     * @return the SMTP port as a string (SMTP_PORT), or null if not defined.
     *         Callers can convert this to an integer if needed.
     */
    public static String getSMTPPort() {
        return dotenv.get("SMTP_PORT");
    }

    /**
     * @return the SMTP TLS flag (SMTP_TLS), typically "true" or "false",
     *         used to configure secure mail transport.
     */
    public static String getSMTPTls() {
        return dotenv.get("SMTP_TLS");
    }

    /**
     * @return the SMTP username (SMTP_USER), or null if not defined.
     */
    public static String getSMTPUser() {
        return dotenv.get("SMTP_USER");
    }

    /**
     * @return the SMTP password (SMTP_PASS), or null if not defined.
     */
    public static String getSMTPPass() {
        return dotenv.get("SMTP_PASS");
    }
}
