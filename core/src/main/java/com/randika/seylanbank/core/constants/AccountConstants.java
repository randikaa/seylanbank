package com.randika.seylanbank.core.constants;

import java.math.BigDecimal;

public class AccountConstants {

    // Account number configuration
    public static final int ACCOUNT_NUMBER_LENGTH = 12;
    public static final String ACCOUNT_NUMBER_PREFIX = "SB";

    // Balance limits
    public static final BigDecimal MINIMUM_ACCOUNT_BALANCE = new BigDecimal("-1000.00");
    public static final BigDecimal DEFAULT_MINIMUM_BALANCE = BigDecimal.ZERO;
    public static final BigDecimal MAXIMUM_ACCOUNT_BALANCE = new BigDecimal("10000000.00");

    // Interest rates (annual percentage)
    public static final BigDecimal DEFAULT_SAVINGS_RATE = new BigDecimal("2.5");
    public static final BigDecimal DEFAULT_CHECKING_RATE = new BigDecimal("0.5");
    public static final BigDecimal DEFAULT_FIXED_DEPOSIT_RATE = new BigDecimal("5.0");
    public static final BigDecimal DEFAULT_CURRENT_RATE = BigDecimal.ZERO;

    // Account types
    public static final String SAVINGS_ACCOUNT = "SAVINGS";
    public static final String CHECKING_ACCOUNT = "CHECKING";
    public static final String FIXED_DEPOSIT_ACCOUNT = "FIXED_DEPOSIT";
    public static final String CURRENT_ACCOUNT = "CURRENT";

    // Currency
    public static final String DEFAULT_CURRENCY = "USD";
    public static final String CURRENCY_SYMBOL = "$";

    // Account status
    public static final String ACTIVE_STATUS = "ACTIVE";
    public static final String INACTIVE_STATUS = "INACTIVE";
    public static final String SUSPENDED_STATUS = "SUSPENDED";
    public static final String CLOSED_STATUS = "CLOSED";
}