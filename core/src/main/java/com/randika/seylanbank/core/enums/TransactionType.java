package com.randika.seylanbank.core.enums;

public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER("Transfer"),
    INTEREST_CREDIT("Interest Credit"),
    FEE_DEBIT("Fee Debit");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCredit() {
        return this == DEPOSIT || this == INTEREST_CREDIT;
    }

    public boolean isDebit() {
        return this == WITHDRAWAL || this == TRANSFER || this == FEE_DEBIT;
    }
}
