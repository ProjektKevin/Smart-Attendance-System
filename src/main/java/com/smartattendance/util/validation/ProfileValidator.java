package com.smartattendance.util.validation;

/**
 * Profile Validator
 * Checks all the input fields for the profile and returns the errors
 * 
 * @author Thiha Swan Htet
 */
public class ProfileValidator {

    /**
     * Validates all profile fields and returns a ValidationResult.
     * Maps each field to its error message (if validation fails).
     *
     * @param firstName the first name input
     * @param lastName  the last name input
     * @param phone     the phone input (optional)
     * @return ValidationResult with field errors mapped
     */
    public static ValidationResult validateProfile(
            String firstName, String lastName, String phone) {

        ValidationResult result = new ValidationResult();

        // Validate firstName
        validateFirstName(firstName, result);

        // Validate lastName
        validateLastName(lastName, result);

        // Validate phone (optional field)
        validatePhone(phone, result);

        return result;
    }

    /**
     * Validates first name using base Validator class.
     * Checks: not empty, min length, max length, only letters with whitespaces
     *
     * @param firstName the first name to validate
     * @param result    the ValidationResult to add errors to
     */
    private static void validateFirstName(String firstName, ValidationResult result) {
        // Check if empty using base Validator
        if (Validator.validateEmptyInput(firstName)) {
            result.addFieldError("firstName", "First name is required");
            return;
        }

        String trimmed = firstName.trim();

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

    /**
     * Validates last name using base Validator class.
     * Checks: not empty, min length, max length, only letters with whitespaces
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
            result.addFieldError("lastName", "Last name must be at least 2 characters");
            return;
        }

        // Check maximum length
        if (!Validator.validateStringMaxChar(25, trimmed)) {
            result.addFieldError("lastName", "Last name cannot exceed 50 characters");
            return;
        }

        // Check if contains only letters with whitespaces
        if (!Validator.validateStringWhiteSpaceOnlyInput(trimmed)) {
            result.addFieldError("lastName", "Last name can only contain letters and spaces");
        }
    }

    /**
     * Validates phone number using base Validator class.
     * Checks: length (if not empty), only integers
     * The phone number is assumed to be regional: Singapore. Thus, 8
     *
     * @param phone  the phone to validate
     * @param result the ValidationResult to add errors to
     */
    private static void validatePhone(String phone, ValidationResult result) {
        // Phone is optional - if empty, that's fine
        if (Validator.validateEmptyInput(phone)) {
            return;
        }

        String trimmed = phone.trim();

        // Check minimum length using base Validator
        if (!Validator.validateExactStringlength(8, trimmed)) {
            result.addFieldError("phone", "Phone must be 8 characters");
            return;
        }

        // Check if the phone number is in integer format
        Integer userPhoneNumber = Validator.validateIntegerInput(trimmed);

        if (userPhoneNumber == null) {
            result.addFieldError("phone", "Phone must be 8 characters");
            return;
        }
    }
}
