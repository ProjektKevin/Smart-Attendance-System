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

/**
 * Service responsible for sending emails (optionally with attachments)
 * using SMTP configuration provided via environment variables.
 *
 * <p>Configuration (read from environment):
 * <ul>
 *     <li>{@code SMTP_HOST} – SMTP server host (default: {@code smtp.gmail.com})</li>
 *     <li>{@code SMTP_PORT} – SMTP server port (default: {@code 587})</li>
 *     <li>{@code SMTP_TLS}  – whether to enable STARTTLS (default: {@code true})</li>
 *     <li>{@code SMTP_USER} – username / email address used for authentication</li>
 *     <li>{@code SMTP_PASS} – password / app password for the SMTP user</li>
 * </ul>
 *
 * <p>The {@code SMTP_USER} value is also used as the {@code From} address for all emails.
 * 
 * @author Ernest Lun
 */
public class EmailService {

    /**
     * Jakarta Mail session configured with SMTP properties and authenticator.
     */
    private final Session session;

    /**
     * Address used as the "From" email in outgoing messages.
     * Derived from the {@code SMTP_USER} environment variable.
     */
    private final String fromAddress;

    /**
     * Constructs an {@code EmailService} using environment variables for SMTP configuration.
     *
     * <p>Sets up:
     * <ul>
     *     <li>SMTP host, port, and TLS settings</li>
     *     <li>Authentication using {@code SMTP_USER} and {@code SMTP_PASS}</li>
     *     <li>Mail {@link Session} with a custom {@link Authenticator}</li>
     * </ul>
     */
    public EmailService() {
        // Read SMTP configuration from environment variables (with sensible defaults).
        String host = getenv("SMTP_HOST", "smtp.gmail.com");
        String port = getenv("SMTP_PORT", "587");
        boolean tls = Boolean.parseBoolean(getenv("SMTP_TLS", "true"));
        String user = getenv("SMTP_USER", "");
        String pass = getenv("SMTP_PASS", "");

        // Use the SMTP user as the "From" address for all emails.
        this.fromAddress = user;

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        // Enable SMTP authentication; required by servers like Gmail.
        props.put("mail.smtp.auth", "true");
        // Enable STARTTLS if configured (recommended for Gmail).
        props.put("mail.smtp.starttls.enable", String.valueOf(tls));
        // Trust the specified host for SSL / TLS connections.
        props.put("mail.smtp.ssl.trust", host);

        // Build mail session with an authenticator that supplies the username and password.
        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
    }

    /**
     * Helper to read an environment variable with a default fallback.
     *
     * @param key environment variable name
     * @param def default value to use if the variable is missing or blank
     * @return resolved value
     */
    private String getenv(String key, String def) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : def;
    }

    /**
     * Main method for sending an email report with optional attachments.
     *
     * <p>Behaviour:
     * <ul>
     *     <li>Creates a {@link MimeMessage} using the configured {@link Session}</li>
     *     <li>Sets the {@code From}, {@code To}, and subject fields</li>
     *     <li>If there are no attachments, sends a simple text email</li>
     *     <li>If there are attachments, builds a multipart message with:
     *         <ul>
     *             <li>One text body part containing {@code body}</li>
     *             <li>Additional body parts, one per file in {@code attachments}</li>
     *         </ul>
     *     </li>
     *     <li>Dispatches the message via {@link Transport#send(Message)}</li>
     * </ul>
     *
     * @param to          recipient email address (comma-separated if multiple)
     * @param subject     email subject line
     * @param body        email body text
     * @param attachments list of files to attach; may be {@code null} or empty
     * @throws Exception if any error occurs while building or sending the message
     */
    public void sendReport(String to, String subject, String body, List<File> attachments) throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromAddress));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        if (attachments == null || attachments.isEmpty()) {
            // Simple text-only email (no attachments).
            message.setText(body);
        } else {
            // Multipart message with text + attachments.
            MimeMultipart multipart = new MimeMultipart();

            // Text part.
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);
            multipart.addBodyPart(textPart);

            // Attachment parts (one per file).
            for (File file : attachments) {
                if (file == null) continue;
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(file);
                attachPart.setFileName(file.getName());
                multipart.addBodyPart(attachPart);
            }

            message.setContent(multipart);
        }

        // Actually send the email using the configured transport.
        Transport.send(message);
    }

    /**
     * Convenience wrapper method that delegates to {@link #sendReport(String, String, String, List)}.
     *
     * <p>Useful when other code is already using a method name like
     * {@code reportService.sendEmail(...)} which, in turn, calls {@code emailService.send(...)}.
     *
     * @param to          recipient email address
     * @param subject     email subject
     * @param body        email body text
     * @param attachments list of files to attach; may be {@code null} or empty
     * @throws Exception if sending fails
     */
    public void send(String to, String subject, String body, List<File> attachments) throws Exception {
        sendReport(to, subject, body, attachments);
    }
}
