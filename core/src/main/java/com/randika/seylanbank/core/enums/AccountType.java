package com.randika.seylanbank.core.enums;

public enum AccountType {
    SAVINGS("Saving Account", 2.5),
    CHECKING("Checking Account", 0.5),
    FIXED_DEPOSIT("Fixed Deposit", 8.0),
    CURRENT("Current Account", 0.0);

    private final String displayName;
    private final double defaultInterestRate;

    AccountType(String displayName, double defaultInterestRate) {
        this.displayName = displayName;
        this.defaultInterestRate = defaultInterestRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDefaultInterestRate() {
        return defaultInterestRate;
    }
}