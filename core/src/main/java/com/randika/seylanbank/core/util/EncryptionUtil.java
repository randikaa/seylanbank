package com.randika.seylanbank.core.util;

import com.randika.seylanbank.core.constants.SecurityConstants;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;

public class EncryptionUtil {

    private static final Logger LOGGER = Logger.getLogger(EncryptionUtil.class.getName());
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    // In production, this should be loaded from a secure key management system
    private static final String DEFAULT_KEY = "MySecretKey12345"; // 16 bytes for AES-128

    public static String encrypt(String data) {
        return encrypt(data, DEFAULT_KEY);
    }

    public static String encrypt(String data, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), SecurityConstants.ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            LOGGER.severe("Error encrypting data: " + e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String encryptedData) {
        return decrypt(encryptedData, DEFAULT_KEY);
    }

    public static String decrypt(String encryptedData, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), SecurityConstants.ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, "UTF-8");

        } catch (Exception e) {
            LOGGER.severe("Error decrypting data: " + e.getMessage());
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(SecurityConstants.ENCRYPTION_ALGORITHM);
            keyGenerator.init(256); // AES-256
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            LOGGER.severe("Error generating encryption key: " + e.getMessage());
            throw new RuntimeException("Key generation failed", e);
        }
    }

    public static String encryptSensitiveData(String data) {
        // For sensitive data like account numbers, SSNs, etc.
        return encrypt(data);
    }

    public static String decryptSensitiveData(String encryptedData) {
        // For sensitive data like account numbers, SSNs, etc.
        return decrypt(encryptedData);
    }

    public static String generateRandomKey(int length) {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[length];
        random.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}