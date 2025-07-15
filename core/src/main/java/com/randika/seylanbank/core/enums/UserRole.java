package com.randika.seylanbank.core.enums;

public enum UserRole {
    SUPER_ADMIN("Super Administrator"),
    ADMIN("Administrator"),
    CUSTOMER("Customer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}