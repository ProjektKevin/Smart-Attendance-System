package com.smartattendance.util.report;

public class EmailSettings {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean tls;
    private final String from;

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

    public static EmailSettings fromEnv() {
        String host = getenvOr("SMTP_HOST", "smtp.gmail.com");
        int port = Integer.parseInt(getenvOr("SMTP_PORT", "587"));
        String user = getenvOr("SMTP_USER", "");
        String pass = getenvOr("SMTP_PASS", "");
        boolean tls = Boolean.parseBoolean(getenvOr("SMTP_TLS", "true"));
        String from = getenvOr("SMTP_FROM", user);

        return new EmailSettings(host, port, user, pass, tls, from);
    }

    private static String getenvOr(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }

    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public boolean isTls() {
        return tls;
    }
    public String getFrom() {
        return from;
    }
}
