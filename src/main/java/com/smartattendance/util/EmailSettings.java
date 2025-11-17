package com.smartattendance.util;

import com.smartattendance.config.ENV;

/**
 * Loads email settings to be passsed into the constructor of the email service
 * 
 * @author Ernest Lun
 */
public record EmailSettings(
        String host, int port, String username, String password, boolean startTls) {

    public static EmailSettings fromEnv() {
        String host = ENV.getSMTPHost();
        int port = Integer.parseInt(ENV.getSMTPPort());
        String user = ENV.getSMTPUser();
        String pass = ENV.getSMTPPass();
        boolean tls = Boolean.parseBoolean(ENV.getSMTPTls());
        return new EmailSettings(host, port, user, pass, tls);
    }
}
