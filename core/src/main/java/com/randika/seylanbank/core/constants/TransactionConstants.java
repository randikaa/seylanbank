package com.randika.seylanbank.core.constants;

import java.math.BigDecimal;

public class TransactionConstants {

    // Transaction limits
    public static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("0.01");
    public static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("100000.00");
    public static final BigDecimal DAILY_TRANSACTION_LIMIT = new BigDecimal("10000.00");
    public static final BigDecimal MONTHLY_TRANSACTION_LIMIT = new BigDecimal("50000.00");

    // Transaction types
    public static final String DEPOSIT = "DEPOSIT";
    public static final String WITHDRAWAL = "WITHDRAWAL";
    public static final String TRANSFER = "TRANSFER";
    public static final String INTEREST_CREDIT = "INTEREST_CREDIT";
    public static final String FEE_DEBIT = "FEE_DEBIT";

    // Transaction status
    public static final String PENDING_STATUS = "PENDING";
    public static final String COMPLETED_STATUS = "COMPLETED";
    public static final String FAILED_STATUS = "FAILED";
    public static final String CANCELLED_STATUS = "CANCELLED";

    // Transaction messages
    public static final String TRANSACTION_SUCCESS = "Transaction completed successfully";
    public static final String TRANSACTION_FAILED = "Transaction failed";
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds";
    public static final String INVALID_AMOUNT = "Invalid transaction amount";
    public static final String ACCOUNT_NOT_FOUND = "Account not found";

    // Fee structure
    public static final BigDecimal WITHDRAWAL_FEE = new BigDecimal("2.00");
    public static final BigDecimal TRANSFER_FEE = new BigDecimal("1.00");
    public static final BigDecimal MAINTENANCE_FEE = new BigDecimal("5.00");

    // Transaction reference patterns
    public static final String TRANSACTION_REF_PREFIX = "TXN";
    public static final int TRANSACTION_REF_LENGTH = 12;
}