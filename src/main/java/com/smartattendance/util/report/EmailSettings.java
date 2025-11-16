package com.smartattendance.util.report;

/**
 * Immutable value object that holds SMTP / email configuration
 * used by the reporting module (e.g. for sending reports via email).
 *
 * <p>This class groups together all connection and sender details:
 * <ul>
 *     <li>SMTP host and port</li>
 *     <li>Username and password for authentication</li>
 *     <li>TLS flag (whether to use STARTTLS / TLS)</li>
 *     <li>Default "from" address</li>
 * </ul>
 *
 * <p>Configuration is typically loaded from environment variables using
 * {@link #fromEnv()}, so the application can be reconfigured without code changes.
 * 
 * @author Ernest Lun
 */
public class EmailSettings {

    /**
     * SMTP server hostname (e.g. "smtp.gmail.com").
     */
    private final String host;

    /**
     * SMTP server port (e.g. 587 for STARTTLS, 465 for SMTPS).
     */
    private final int port;

    /**
     * Username for authenticating with the SMTP server.
     */
    private final String username;

    /**
     * Password or app-specific password for authenticating with the SMTP server.
     */
    private final String password;

    /**
     * Whether to use TLS (e.g. STARTTLS) when connecting.
     */
    private final boolean tls;

    /**
     * Default "From" email address to use when sending messages.
     */
    private final String from;

    /**
     * Create a new {@code EmailSettings} instance with all fields explicitly provided.
     *
     * @param host     SMTP host name
     * @param port     SMTP port
     * @param username username for authentication
     * @param password password for authentication
     * @param tls      whether to use TLS
     * @param from     default "From" address
     */
    public EmailSettings(String host,
                         int port,
                         String username,
                         String password,
                         boolean tls,
                         String from) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.tls = tls;
        this.from = from;
    }

    /**
     * Build an {@code EmailSettings} instance from environment variables,
     * falling back to sensible defaults when variables are not set.
     *
     * <p>Environment variables used:
     * <ul>
     *     <li>{@code SMTP_HOST} → host (default: {@code "smtp.gmail.com"})</li>
     *     <li>{@code SMTP_PORT} → port (default: {@code "587"})</li>
     *     <li>{@code SMTP_USER} → username (default: empty string)</li>
     *     <li>{@code SMTP_PASS} → password (default: empty string)</li>
     *     <li>{@code SMTP_TLS} → TLS flag (default: {@code "true"})</li>
     *     <li>{@code SMTP_FROM} → from address (default: {@code SMTP_USER})</li>
     * </ul>
     *
     * @return a fully constructed {@code EmailSettings} instance based on the environment
     */
    public static EmailSettings fromEnv() {
        String host = getenvOr("SMTP_HOST", "smtp.gmail.com");
        int port = Integer.parseInt(getenvOr("SMTP_PORT", "587"));
        String user = getenvOr("SMTP_USER", "");
        String pass = getenvOr("SMTP_PASS", "");
        boolean tls = Boolean.parseBoolean(getenvOr("SMTP_TLS", "true"));
        String from = getenvOr("SMTP_FROM", user);

        return new EmailSettings(host, port, user, pass, tls, from);
    }

    /**
     * Helper to read an environment variable with a fallback default.
     *
     * <p>If the environment variable is not set or is blank,
     * the {@code fallback} value is returned instead.
     *
     * @param key      name of the environment variable
     * @param fallback value to return if the environment variable is missing or blank
     * @return the environment value or the fallback if not present
     */
    private static String getenvOr(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }

    /**
     * @return the SMTP host name
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the SMTP port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the SMTP username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the SMTP password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return {@code true} if TLS should be used, {@code false} otherwise
     */
    public boolean isTls() {
        return tls;
    }

    /**
     * @return the default "From" email address
     */
    public String getFrom() {
        return from;
    }
}
