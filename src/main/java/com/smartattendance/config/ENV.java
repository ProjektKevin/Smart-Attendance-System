package com.smartattendance.config;

import io.github.cdimascio.dotenv.Dotenv;

public class ENV {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getDatabaseURL() {
        return dotenv.get("DATABASE_URL");
    }

    public static String getDatabaseUser() {
        return dotenv.get("DATABASE_USER");
    }

    public static String getDatabasePassword() {
        return dotenv.get("DATABASE_PASSWORD");
    }

    public static String getSMTPHost() {
        return dotenv.get("SMTP_HOST");
    }

    public static String getSMTPPort() {
        return dotenv.get("SMTP_PORT");
    }

    public static String getSMTPTls() {
        return dotenv.get("SMTP_TLS");
    }

    public static String getSMTPUser() {
        return dotenv.get("SMTP_USER");
    }

    public static String getSMTPPass() {
        return dotenv.get("SMTP_PASS");
    }
}
