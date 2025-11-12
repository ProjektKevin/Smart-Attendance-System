package com.smartattendance.service;

import java.io.File;
import java.util.List;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class EmailService {

    private final Session session;
    private final String fromAddress;

    public EmailService() {
        // read from ENV 
        String host = getenv("SMTP_HOST", "smtp.gmail.com");
        String port = getenv("SMTP_PORT", "587");
        boolean tls = Boolean.parseBoolean(getenv("SMTP_TLS", "true"));
        String user = getenv("SMTP_USER", "");
        String pass = getenv("SMTP_PASS", "");

        this.fromAddress = user; // we’ll send FROM this address

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");                // <— REQUIRED or you get 530
        props.put("mail.smtp.starttls.enable", String.valueOf(tls));
        props.put("mail.smtp.ssl.trust", host);

        // build session with authenticator so Gmail actually gets USER/PASS
        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
    }

    private String getenv(String key, String def) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : def;
    }

    /**
     * Main method report service can call.
     * If you already call sendReport(...) keep this name.
     */
    public void sendReport(String to, String subject, String body, List<File> attachments) throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromAddress));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        if (attachments == null || attachments.isEmpty()) {
            // simple text mail
            message.setText(body);
        } else {
            // multipart with attachments
            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);
            multipart.addBodyPart(textPart);

            for (File file : attachments) {
                if (file == null) continue;
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(file);
                attachPart.setFileName(file.getName());
                multipart.addBodyPart(attachPart);
            }

            message.setContent(multipart);
        }

        // actually send
        Transport.send(message);
    }

    /**
     * If other code calls reportService.sendEmail(...) and that in turn
     * calls emailService.send(...), you can keep a thin wrapper like this.
     */
    public void send(String to, String subject, String body, List<File> attachments) throws Exception {
        sendReport(to, subject, body, attachments);
    }
}
