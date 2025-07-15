package com.randika.seylanbank.core.constants;

public class SecurityConstants {

    // User roles
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String CUSTOMER_ROLE = "CUSTOMER";

    // Authentication schemes
    public static final String FORM_AUTH = "FORM";
    public static final String BASIC_AUTH = "BASIC";
    public static final String DIGEST_AUTH = "DIGEST";

    // Session configuration
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final int ACCOUNT_LOCKOUT_DURATION_MINUTES = 15;

    // Password policy
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 50;
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    // Security headers
    public static final String CONTENT_TYPE_OPTIONS = "nosniff";
    public static final String FRAME_OPTIONS = "DENY";
    public static final String XSS_PROTECTION = "1; mode=block";
    public static final String HSTS_HEADER = "max-age=31536000; includeSubDomains";

    // Encryption
    public static final String HASH_ALGORITHM = "SHA-256";
    public static final String ENCRYPTION_ALGORITHM = "AES";
    public static final int SALT_LENGTH = 32;

    // JNDI names
    public static final String DATASOURCE_JNDI = "java:jboss/datasources/SeylanBankDS";
    public static final String SECURITY_DOMAIN = "SeylanBankSecurityDomain";
}