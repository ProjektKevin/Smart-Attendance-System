package com.smartattendance.util.validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds validation results for multiple fields.
 * Maps field names to their error messages (if any).
 * @author Thiha Swan Htet
 * Inspiration: Zod Validation Library
 * https://zod.dev/
 */
public class ValidationResult {
    private boolean isValid = true;
    private Map<String, String> fieldErrors = new HashMap<>();

    /**
     * Adds an error message for a specific field.
     *
     * @param fieldName    the name of the field with error
     * @param errorMessage the error message to display
     */
    public void addFieldError(String fieldName, String errorMessage) {
        fieldErrors.put(fieldName, errorMessage);
        isValid = false;
    }

    /**
     * Checks if all validations passed.
     *
     * @return true if no errors, false if any errors exist
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Gets the error message for a specific field.
     *
     * @param fieldName the field to get error for
     * @return error message, or null if field has no error
     */
    public String getFieldError(String fieldName) {
        return fieldErrors.get(fieldName);
    }

    /**
     * Gets all field errors as a map.
     * Used to display errors under each field in UI.
     *
     * @return map of fieldName -> errorMessage
     */
    public Map<String, String> getAllFieldErrors() {
        return fieldErrors;
    }

}
