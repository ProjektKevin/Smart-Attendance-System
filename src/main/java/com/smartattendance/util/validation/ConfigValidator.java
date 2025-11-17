package com.smartattendance.util.validation;

/**
 * Config Validator
 * Checks all the input fields for the configuration matters
 * Used for when user update configuration properties in settings
 * 
 * @author Min Thet Khine
 */
public class ConfigValidator {
  /**
   * Validates all configuration settings
   * Checks: camera index, thresholds, cooldown time, database path
   *
   * @param cameraIndex   the camera index to validate
   * @param highThreshold the high threshold to validate
   * @param lowThreshold  the low threshold to validate
   * @param cooldownTime  the cooldown time to validate
   * @param databasePath  the database path to validate
   * @return ValidationResult containing all validation errors
   */
  public static ValidationResult validateConfig(String cameraIndex, String highThreshold, String lowThreshold,
      String cooldownTime, String databasePath) {

    ValidationResult result = new ValidationResult();

    validateCameraIndex(cameraIndex, result);

    validateThresholds(highThreshold, lowThreshold, result);

    validateCooldownTime(cooldownTime, result);

    validateDatabasePath(databasePath, result);

    return result;
  }

  /**
   * Validates camera index
   * Checks: not empty, valid integer, non-negative (>= 0)
   *
   * @param cameraIndex the camera index to validate
   * @param result      the ValidationResult to add errors to
   */
  public static void validateCameraIndex(String cameraIndex, ValidationResult result) {
    // Check if empty
    if (Validator.validateEmptyInput(cameraIndex)) {
      result.addFieldError("cameraIndex", "Camera index is required");
      return;
    }

    String trimmed = cameraIndex.trim();

    // Try to parse as integer
    int cameraValue;
    try {
      cameraValue = Integer.parseInt(trimmed);
    } catch (NumberFormatException e) {
      result.addFieldError("cameraIndex", "Camera index must be a valid number");
      return;
    }

    // Validate non-negative
    if (cameraValue < 0) {
      result.addFieldError("cameraIndex", "Camera index must be 0 or greater (You entered: " + cameraValue + ")");
      return;
    }
  }

  /**
   * Validates recognition thresholds
   * Checks: not empty, valid numbers, within range (0-100), high > low, at least 1 point apart
   *
   * @param highThreshold the high threshold to validate
   * @param lowThreshold  the low threshold to validate
   * @param result        the ValidationResult to add errors to
   */
  public static void validateThresholds(String highThreshold, String lowThreshold, ValidationResult result) {
    if (Validator.validateEmptyInput(highThreshold)) {
      result.addFieldError("highThreshold", "High threshold is required");
      return;
    }

    // Validate low threshold
    if (Validator.validateEmptyInput(lowThreshold)) {
      result.addFieldError("lowThreshold", "Low threshold is required");
      return;
    }

    String trimmedHigh = highThreshold.trim();
    String trimmedLow = lowThreshold.trim();

    // Try to parse as doubles
    double highValue;
    double lowValue;

    try {
      highValue = Double.parseDouble(trimmedHigh);
    } catch (NumberFormatException e) {
      result.addFieldError("highThreshold", "High threshold must be a valid number");
      return;
    }

    try {
      lowValue = Double.parseDouble(trimmedLow);
    } catch (NumberFormatException e) {
      result.addFieldError("lowThreshold", "Low threshold must be a valid number");
      return;
    }

    // Validate range for high threshold (0-100)
    if (highValue < 0 || highValue > 100) {
      result.addFieldError("highThreshold",
          "High threshold must be between 0 and 100 (You entered: " + highValue + ")");
      return;
    }

    // Validate range for low threshold (0-100)
    if (lowValue < 0 || lowValue > 100) {
      result.addFieldError("lowThreshold",
          "Low threshold must be between 0 and 100 (You entered: " + lowValue + ")");
      return;
    }

    // Validate that high threshold is greater than low threshold
    if (highValue <= lowValue) {
      result.addFieldError("thresholds", "High threshold must be greater than low threshold");
      return;
    }

    // Validate that they are at least 1 point apart
    if (highValue - lowValue < 1.0) {
      result.addFieldError("thresholds", "High and low thresholds must be at least 1 point apart");
      return;
    }
  }

  /**
   * Validates cooldown time
   * Checks: not empty, valid integer, non-negative
   *
   * @param cooldownTime the cooldown time to validate
   * @param result       the ValidationResult to add errors to
   */
  public static void validateCooldownTime(String cooldownTime, ValidationResult result) {
    // Check if empty
    if (Validator.validateEmptyInput(cooldownTime)) {
      result.addFieldError("cooldown", "Cooldown time is required");
      return;
    }

    String trimmed = cooldownTime.trim();

    // Try to parse as integer
    int cooldownValue;
    try {
      cooldownValue = Integer.parseInt(trimmed);
    } catch (NumberFormatException e) {
      result.addFieldError("cooldown", "Cooldown time must be a valid number");
      return;
    }

    // Validate non-negative
    if (cooldownValue < 0) {
      result.addFieldError("cooldown", "Cooldown must be 0 or greater (You entered: " + cooldownValue + ")");
      return;
    }
  }

  /**
   * Validates database path
   * Checks: not empty, valid path format, file extension
   *
   * @param databasePath the database path to validate
   * @param result       the ValidationResult to add errors to
   */
  public static void validateDatabasePath(String databasePath, ValidationResult result) {
    // Check if empty
    if (Validator.validateEmptyInput(databasePath)) {
      result.addFieldError("databasePath", "Database path is required");
      return;
    }

    String trimmed = databasePath.trim();

    // Check minimum length
    if (!Validator.validateStringMinChar(3, trimmed)) {
      result.addFieldError("databasePath", "Database path must be at least 3 characters");
      return;
    }

    // Check if path has valid extension (optional but recommended)
    if (!trimmed.toLowerCase().endsWith(".db") &&
        !trimmed.toLowerCase().endsWith(".sqlite") &&
        !trimmed.toLowerCase().endsWith(".sqlite3")) {
      result.addFieldError("databasePath",
          "Database path must end with .db, .sqlite, or .sqlite3");
      return;
    }

    // Check for invalid characters in path
    if (trimmed.contains("<") || trimmed.contains(">") ||
        trimmed.contains("|") || trimmed.contains("\"")) {
      result.addFieldError("databasePath", "Database path contains invalid characters");
      return;
    }
  }
}