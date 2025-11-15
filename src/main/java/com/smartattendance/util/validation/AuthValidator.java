package com.smartattendance.util.validation;

import com.smartattendance.model.enums.Role;

/**
 * Auth Validator
 * Checks all the input fields for the authentication related matters
 * Used for user invitation (admin), login, registration, forgot password, token
 * verification
 * 
 * @author Thiha Swan Htet
 */
public class AuthValidator {
    public static ValidationResult validateAddSingleStudent(String email, String role) {
        ValidationResult result = new ValidationResult();

        // Validate email
        validateEmail(email, result);

        // Validate role
        validateRole(role, result);

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

    public static ValidationResult validateForgotPassword(String email) {
        ValidationResult result = new ValidationResult();

        // Validate email
        validateEmail(email, result);

        return result;
    }

    public static ValidationResult validateVerification(String token) {
        ValidationResult result = new ValidationResult();

        // Validate token
        validateToken(token, result);

        return result;
    }

    public static ValidationResult validateRegistration(String username, String firstName, String lastName,
            String password, String confirmPassword) {
        ValidationResult result = new ValidationResult();

        // Validate username
        validateRegistrationUsername(username, result);

        // Validate first name
        validateFirstName(firstName, result);

        // Validate last name
        validateLastName(lastName, result);

        // Validate password
        validateRegistrationPassword(password, result);

        // Validate password confirmation
        validateConfirmPassword(password, confirmPassword, result);

        return result;
    }

    public static ValidationResult validatePasswordReset(String newPassword,
            String confirmPassword) {
        ValidationResult result = new ValidationResult();

        // Validate new password
        validateRegistrationPassword(newPassword, result);

        // Validate password confirmation
        validateConfirmPassword(newPassword, confirmPassword, result);

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

        /*
         * Check email format with regex
         * Source referenced from zod library for email validation
         * Link: https://colinhacks.com/essays/reasonable-email-regex
         */
        String regexPattern = "/^(?!\\.)(?!.*\\.\\.)([a-z0-9_'+\\-\\.]*)[a-z0-9_'+\\-]@([a-z0-9][a-z0-9\\-]*\\.)+[a-z]{2,}$/i";
        if (Validator.validateWithRegex(regexPattern, trimmed)) {
            result.addFieldError("email", "Invalid email format.");
            return;
        }
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
     * Validates role
     * Checks: not empty, and role ENUM: Admin, Student
     *
     * @param role   the role to validate
     * @param result the ValidationResult to add errors to
     */
    private static void validateRole(String role, ValidationResult result) {
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

    /**
     * Validates token for verification
     * Checks: not empty, min put, max input
     *
     * @param token  the token to validate
     * @param result the ValidationResult to add errors to
     */
    private static void validateToken(String token, ValidationResult result) {
        // Check if empty using base Validator
        if (Validator.validateEmptyInput(token)) {
            result.addFieldError("token", "Token is required");
            return;
        }

        String trimmed = token.trim();

        // Check minimum length using base Validator
        if (!Validator.validateStringMinChar(5, trimmed)) {
            result.addFieldError("token", "Token is required");
            return;
        }

        // Check maximum length using base Validator
        if (!Validator.validateStringMaxChar(32, trimmed)) {
            result.addFieldError("token", "Token exceeded limits");
            return;
        }

    }

    /**
     * Validates username for registration
     * Checks: not empty, min length (3), max length (10), alphanumeric only
     *
     * @param username the username to validate
     * @param result   the ValidationResult to add errors to
     */
    private static void validateRegistrationUsername(String username, ValidationResult result) {
        // Check if empty
        if (Validator.validateEmptyInput(username)) {
            result.addFieldError("username", "Username is required");
            return;
        }

        String trimmed = username.trim();

        // Check minimum length
        if (!Validator.validateStringMinChar(3, trimmed)) {
            result.addFieldError("username", "Username must be at least 3 characters");
            return;
        }

        // Check maximum length
        if (!Validator.validateStringMaxChar(10, trimmed)) {
            result.addFieldError("username", "Username must not exceed 10 characters");
            return;
        }

        // Check letters and digits only
        if (!Validator.validateContainsLetterAndDigit(trimmed)) {
            result.addFieldError("username", "Username must contain only letters and numbers");
            return;
        }
    }

    /**
     * Validates first name for registration
     * Checks: not empty, min length (3), max length (25), only letters and
     * whitespace
     *
     * @param firstName the first name to validate
     * @param result    the ValidationResult to add errors to
     */
    private static void validateFirstName(String firstName, ValidationResult result) {
        // Check if empty
        if (Validator.validateEmptyInput(firstName)) {
            result.addFieldError("firstName", "First name is required");
            return;
        }

        String trimmed = firstName.trim();

        // Check minimum length
        if (!Validator.validateStringMinChar(3, trimmed)) {
            result.addFieldError("firstName", "First name must be at least 3 characters");
            return;
        }

        // Check maximum length
        if (!Validator.validateStringMaxChar(25, trimmed)) {
            result.addFieldError("firstName", "First name must not exceed 25 characters");
            return;
        }

        // Check only letters and whitespace
        if (!Validator.validateStringWhiteSpaceOnlyInput(trimmed)) {
            result.addFieldError("firstName", "First name must contain only letters");
            return;
        }
    }

    /**
     * Validates last name for registration
     * Checks: not empty, min length (3), max length (25), only letters and
     * whitespace
     *
     * @param lastName the last name to validate
     * @param result   the ValidationResult to add errors to
     */
    private static void validateLastName(String lastName, ValidationResult result) {
        // Check if empty
        if (Validator.validateEmptyInput(lastName)) {
            result.addFieldError("lastName", "Last name is required");
            return;
        }

        String trimmed = lastName.trim();

        // Check minimum length
        if (!Validator.validateStringMinChar(3, trimmed)) {
            result.addFieldError("lastName", "Last name must be at least 3 characters");
            return;
        }

        // Check maximum length
        if (!Validator.validateStringMaxChar(25, trimmed)) {
            result.addFieldError("lastName", "Last name must not exceed 25 characters");
            return;
        }

        // Check only letters and whitespace
        if (!Validator.validateStringWhiteSpaceOnlyInput(trimmed)) {
            result.addFieldError("lastName", "Last name must contain only letters");
            return;
        }
    }

    /**
     * Validates password for registration
     * Checks: not empty, min length (8), max length (32), at least one uppercase,
     * one lowercase, one digit
     *
     * @param password the password to validate
     * @param result   the ValidationResult to add errors to
     */
    private static void validateRegistrationPassword(String password, ValidationResult result) {
        // Check if empty
        if (Validator.validateEmptyInput(password)) {
            result.addFieldError("newPassword", "Password is required");
            return;
        }

        String trimmed = password.trim();

        // Check minimum length
        if (!Validator.validateStringMinChar(8, trimmed)) {
            result.addFieldError("newPassword", "Password must be at least 8 characters");
            return;
        }

        // Check maximum length
        if (!Validator.validateStringMaxChar(32, trimmed)) {
            result.addFieldError("newPassword", "Password must not exceed 32 characters");
            return;
        }

        // Check for at least one uppercase letter
        if (!Validator.validateAtLeastOneUpperCase(trimmed)) {
            result.addFieldError("newPassword", "Password must contain at least one uppercase letter");
            return;
        }

        // Check for at least one lowercase letter
        if (!Validator.validateAtLeastOneLowerCase(trimmed)) {
            result.addFieldError("newPassword", "Password must contain at least one lowercase letter");
            return;
        }

        // Check for at least one digit
        if (!Validator.validateIsDigit(trimmed)) {
            result.addFieldError("newPassword", "Password must contain at least one number");
            return;
        }
    }

    /**
     * Validates password confirmation matches new password
     *
     * @param password        the new password
     * @param confirmPassword the confirmation password
     * @param result          the ValidationResult to add errors to
     */
    private static void validateConfirmPassword(String password, String confirmPassword, ValidationResult result) {
        // Check if empty
        if (Validator.validateEmptyInput(confirmPassword)) {
            result.addFieldError("confirmPassword", "Please confirm your password");
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            result.addFieldError("confirmPassword", "Passwords do not match");
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
