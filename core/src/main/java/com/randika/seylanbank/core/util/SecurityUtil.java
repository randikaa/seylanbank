package com.randika.seylanbank.core.util;

import com.randika.seylanbank.core.constants.SecurityConstants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SecurityUtil {

    private static final Logger LOGGER = Logger.getLogger(SecurityUtil.class.getName());
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(SecurityConstants.PASSWORD_PATTERN);

    public static String hashPassword(String password) {
        return hashPassword(password, generateSalt());
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(SecurityConstants.HASH_ALGORITHM);
            md.update(salt.getBytes());
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe("Hashing algorithm not found: " + e.getMessage());
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    public static String generateSalt() {
        byte[] salt = new byte[SecurityConstants.SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static boolean verifyPassword(String password, String hashedPassword, String salt) {
        String hashedInput = hashPassword(password, salt);
        return hashedInput.equals(hashedPassword);
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }

        if (password.length() < SecurityConstants.MIN_PASSWORD_LENGTH ||
                password.length() > SecurityConstants.MAX_PASSWORD_LENGTH) {
            return false;
        }

        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static String generateSessionToken() {
        byte[] token = new byte[32];
        SECURE_RANDOM.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'%;()&+]", "").trim();
    }

    public static boolean isValidRole(String role) {
        return SecurityConstants.SUPER_ADMIN_ROLE.equals(role) ||
                SecurityConstants.ADMIN_ROLE.equals(role) ||
                SecurityConstants.CUSTOMER_ROLE.equals(role);
    }

    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "*".repeat(accountNumber.length() - 4) + accountNumber.substring(accountNumber.length() - 4);
    }
}