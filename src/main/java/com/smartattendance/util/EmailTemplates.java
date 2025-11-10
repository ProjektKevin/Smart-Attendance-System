package com.smartattendance.util;

/**
 * Email template utility class
 * Provides pre-formatted email templates for various authentication flows
 *
 * chore(), Harry: Map back to HTML templates if got time
 */
public class EmailTemplates {

    /**
     * Forgot Password email template
     * Sent when user requests password recovery
     *
     * @param username The first name of the recipient
     * @param token    The verification token to include in email
     * @return HTML/Text body of the forgot password email
     */
    public static String forgotPasswordEmail(String username, String token) {
        return String.format("""
                Dear %s,

                We received a request to reset your password for your Smart Attendance System account.

                To proceed with your password reset, please use the following verification token:

                ═══════════════════════════════════
                   %s
                ═══════════════════════════════════

                This token will expire in 15 minutes.

                If you did not request a password reset, please ignore this email.

                Important Security Notes:
                - Never share this token with anyone
                - Our team will never ask for this token via email
                - If you did not initiate this request, your account may be compromised

                Need help? Contact our support team at support@smartattendance.com

                Best regards,
                Smart Attendance System Team
                """, username, token);
    }

    /**
     * Email Verification template
     * Sent to unverified email addresses during signup or forgot password flow
     *
     * @param username The first name of the recipient
     * @param token    The verification token to include in email
     * @return HTML/Text body of the verification email
     */
    public static String verificationEmail(String token) {
        return String.format("""
                Dear User,

                Welcome to Smart Attendance System!

                To complete your registration, please verify your email address using the following token:

                ═══════════════════════════════════
                   %s
                ═══════════════════════════════════

                This token will expire in 30 minutes.

                Steps to verify:
                1. Copy the token above
                2. Return to the application
                3. Paste the token in the verification field
                4. Click Submit

                If you did not create an account with us, please contact our support team.

                Need help? Contact our support team at support@smartattendance.com

                Best regards,
                Smart Attendance System Team
                """, token);
    }
}
