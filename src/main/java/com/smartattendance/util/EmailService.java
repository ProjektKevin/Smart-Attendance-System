package com.smartattendance.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

public class EmailService {
    private final EmailSettings settings;

    public EmailService(EmailSettings settings) {
        this.settings = settings;
    }

    public void send(String to, String subject, String body, List<File> attachments) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(settings.startTls()));
        props.put("mail.smtp.host", settings.host());
        props.put("mail.smtp.port", String.valueOf(settings.port()));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(settings.username(), settings.password());
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(settings.username()));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject, StandardCharsets.UTF_8.name());

        // Text part
        MimeBodyPart text = new MimeBodyPart();
        text.setText(body, StandardCharsets.UTF_8.name());

        // Attachments
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(text);
        if (attachments != null) {
            for (File f : attachments) {
                if (f != null && f.exists()) {
                    MimeBodyPart a = new MimeBodyPart();
                    a.attachFile(f);
                    mp.addBodyPart(a);
                }
            }
        }
        msg.setContent(mp);
        Transport.send(msg);
    }
}
