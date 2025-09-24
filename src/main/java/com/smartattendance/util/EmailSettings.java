package com.smartattendance.util;

public record EmailSettings(
        String host, int port, String username, String password, boolean startTls) {

    public static EmailSettings fromEnv() {
        String host = getenv("SMTP_HOST", "smtp.gmail.com");
        int port = Integer.parseInt(getenv("SMTP_PORT", "587"));
        String user = getenv("SMTP_USER", "");
        String pass = getenv("SMTP_PASS", ""); // use an App Password for Gmail/Outlook
        boolean tls = Boolean.parseBoolean(getenv("SMTP_TLS", "true"));
        return new EmailSettings(host, port, user, pass, tls);
    }

    private static String getenv(String k, String d) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? d : v.trim();
    }
}
