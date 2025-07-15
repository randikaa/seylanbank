package com.randika.seylanbank.core.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[1-9]\\d{1,14}$|^\\d{10}$"
    );

    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(
            "^\\d{12}$"
    );

    private static final Pattern NATIONAL_ID_PATTERN = Pattern.compile(
            "^\\d{9}[vVxX]$|^\\d{12}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        // Remove all non-digit characters except +
        String cleanedPhone = phoneNumber.replaceAll("[^+\\d]", "");
        return PHONE_PATTERN.matcher(cleanedPhone).matches();
    }

    public static boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        return ACCOUNT_NUMBER_PATTERN.matcher(accountNumber.trim()).matches();
    }

    public static boolean isValidNationalId(String nationalId) {
        if (nationalId == null || nationalId.trim().isEmpty()) {
            return false;
        }
        return NATIONAL_ID_PATTERN.matcher(nationalId.trim()).matches();
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isAlphabetic(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return value.matches("^[a-zA-Z\\s]+$");
    }

    public static boolean isAlphanumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return value.matches("^[a-zA-Z0-9\\s]+$");
    }

    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    public static String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("[<>\"'&]", "");
    }

    public static boolean isValidPostalCode(String postalCode) {
        if (postalCode == null || postalCode.trim().isEmpty()) {
            return false;
        }
        // Simple pattern for various postal code formats
        return postalCode.matches("^\\d{5}(-\\d{4})?$|^[A-Z]\\d[A-Z] \\d[A-Z]\\d$|^\\d{5}$");
    }
}