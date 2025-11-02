package com.smartattendance.util.validation;

public class UserValidator {
    public static ValidationResult validateAddSingleStudent(String email) {
        ValidationResult result = new ValidationResult();

        // Validate email
        validateEmail(email, result);

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
            result.addFieldError("email", "First name is required");
            return;
        }

        String trimmed = email.trim();

        // Check minimum length using base Validator
        if (!Validator.validateStringMinChar(3, trimmed)) {
            result.addFieldError("firstName", "First name must be at least 2 characters");
            return;
        }

        // Check maximum length using base Validator
        if (!Validator.validateStringMaxChar(25, trimmed)) {
            result.addFieldError("firstName", "First name cannot exceed 50 characters");
            return;
        }

        // Check if contains only letters with whitespaces
        if (!Validator.validateStringWhiteSpaceOnlyInput(trimmed)) {
            result.addFieldError("firstName", "First name can only contain letters and spaces");
        }
    }
}
