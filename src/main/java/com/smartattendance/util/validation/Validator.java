package com.smartattendance.util.validation;

/**
 * Validator Util to validate input fields
 * Base general validations
 * 
 * @author Thiha Swan Htet
 */
public class Validator {

    /**
     * Method to validate if the user input is null
     *
     * @param input the user input string
     * @return true if empty, false if not
     */
    public static boolean validateEmptyInput(String input) {
        return input == null || input.isBlank();
    }

    /**
     * Method to validate minimum characters
     *
     * @param min   the min characters expected
     * @param input the user input string
     * 
     * @return true if minimun character limit or more, false if not
     */
    public static boolean validateStringMinChar(int min, String input) {
        return input.length() >= min;
    }

    /**
     * Method to validate maximum characters
     *
     * @param max   the max characters expected
     * @param input the user input string
     * 
     * @return true if within maximum character limit, false if not
     */
    public static boolean validateStringMaxChar(int max, String input) {
        return input.length() <= max;
    }

    /**
     * Method to check the exact unit limit of the user input.
     *
     * @param limit the max characters expected
     * @param input the user input string
     * 
     * @return true if correct, false if not
     */
    public static boolean validateExactStringlength(int limit, String input) {
        return input.length() == limit;
    }

    /**
     * Method to validate if the user input is integer
     *
     * @param input the user input string
     * 
     * @return Integer if of integer type, null if not
     */
    public static Integer validateIntegerInput(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Method to check if the string consists only letters.
     *
     * @param start the starting index of the input
     * @param end   the ending index of the input
     * @param input the user input string
     * 
     * @return true if the string is all letters, false if not
     */
    public static boolean validateIsLetter(
            int start, int end, String input) {
        try {
            char[] characters = input.toCharArray();
            for (int i = start; i < end; i++) {
                if (!Character.isLetter(characters[i])) {
                    return false;
                }
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Method to check if the string is alpha numeric
     *
     * @param input the string input
     * 
     * @return true if the string contains both letters and digits, false if not
     */
    public static boolean validateContainsLetterAndDigit(String input) {
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : input.toCharArray()) {
            if (Character.isLetter(c))
                hasLetter = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            if (hasLetter && hasDigit)
                return true;
        }
        return false;
    }

    /**
     * Method to validate the given string consists only letters and whitespace.
     *
     * @param input the user input string
     * 
     * @return true if correct, false if not
     */
    public static boolean validateStringWhiteSpaceOnlyInput(String input) {

        // Break down string text to individual letter/character
        char[] characters = input.toCharArray();
        for (char c : characters) {
            // Checks if character "c" is letter and contains white space
            if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method to check if the string contains at least one uppercase letter
     *
     * @param input the string input
     * 
     * @return true if the string is if uppercase >= 1, false if not
     */
    public static boolean validateAtLeastOneUpperCase(String input) {
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to check if the string contains at least one lowercase letter
     *
     * @param input the string input
     * 
     * @return true if the string is if lowercase >= 1, false if not
     */
    public static boolean validateAtLeastOneLowerCase(String input) {
        for (char c : input.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to check if the string contains at least one digit
     *
     * @param input the string input
     * 
     * @return true if the string is contains at least one digit
     */
    public static boolean validateIsDigit(String input) {
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }
}
