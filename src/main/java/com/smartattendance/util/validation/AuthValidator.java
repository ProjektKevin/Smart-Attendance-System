package com.smartattendance.util.validation;

import com.smartattendance.model.enums.Role;

public class AuthValidator {
    public static ValidationResult validateAddSingleStudent(String email, String role) {
        ValidationResult result = new ValidationResult();

        // Validate email
        validateEmail(email, result);

        // Validate role
        valideRole(role, result);

        return result;
    }

    public static ValidationResult validateLogin(String username, String password) {
        ValidationResult result = new ValidationResult();

        // Validate username
        validateLoginUserName(username, result);

        // Validate password
        validateLoginPassword(password, result);

        return result;
    }

    /**
     * Validates email
     * Checks: not empty, min length, max length, only letters with whitespaces
     *
     * @param email  the email to validate
     * @param result the ValidationResult to add errors to
     */
    private static void validateEmail(String email, ValidationResult result) {
        // Check if empty using base Validator
        if (Validator.validateEmptyInput(email)) {
            result.addFieldError("email", "Email is required");
            return;
        }

        String trimmed = email.trim();

        // chore(), Harry: Check email format with regex
    }

    /**
     * Validates username using base Validator class.
     * Checks: not empty max length only
     * Removes: min characters, alpha numeric checking
     * For security reasons, login will not show detailed errors
     *
     * @param username the first name to validate
     * @param result   the ValidationResult to add errors to
     */
    private static void validateLoginUserName(String username, ValidationResult result) {
        // Check if empty using base Validator
        if (Validator.validateEmptyInput(username)) {
            result.addFieldError("username", "Username is required");
            return;
        }

        String trimmed = username.trim();

        // Check maximum length using base Validator
        if (!Validator.validateStringMaxChar(25, trimmed)) {
            result.addFieldError("username", "Username exceeded limits");
            return;
        }
    }

    /**
     * Validates password using base Validator class.
     * Checks: not empty max length only
     * Removes: min characters, alpha numeric checking
     * For security reasons, login will not show detailed errors
     * Though both username and password login validate function performs similarly,
     * it is
     * left duplicated for expansion if in case more restrictions are needed
     *
     * @param password the password to validate
     * @param result   the ValidationResult to add errors to
     */
    private static void validateLoginPassword(String password, ValidationResult result) {
        // Check if empty using base Validator
        if (Validator.validateEmptyInput(password)) {
            result.addFieldError("password", "Password is required");
            return;
        }

        String trimmed = password.trim();

        // Check maximum length using base Validator
        if (!Validator.validateStringMaxChar(32, trimmed)) {
            result.addFieldError("password", "Password exceeded limits");
            return;
        }
    }

    /**
     * Validates email
     * Checks: not empty, min length, max length, only letters with whitespaces
     *
     * @param email  the email to validate
     * @param result the ValidationResult to add errors to
     */
    private static void valideRole(String role, ValidationResult result) {
        // Check if empty using base Validator
        if (Validator.validateEmptyInput(role)) {
            result.addFieldError("role", "Role is required");
            return;
        }

        String trimmed = role.trim();

        if (!isRoleValid(trimmed)) {
            result.addFieldError("role", "Role is invalid");
            return;
        }
    }

    // Check if role is valid by enum
    public static boolean isRoleValid(String role) {
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
}
