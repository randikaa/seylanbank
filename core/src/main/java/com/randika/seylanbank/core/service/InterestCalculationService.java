package com.randika.seylanbank.core.service;

import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.enums.AccountType;

import jakarta.ejb.Remote;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Remote
public interface InterestCalculationService {
    void calculateInterestForAccount(Long accountId);
    void calculateInterestForAllAccounts();
    BigDecimal getInterestAmount(Long accountId, Date fromDate, Date toDate);

    // Customer methods
    BigDecimal getMyInterestAmount(Long accountId, Date fromDate, Date toDate, Long customerId) throws Exception;

    // Admin methods
    void updateInterestRates(AccountType accountType, BigDecimal newRate);
    List<Account> getHighInterestAccounts(BigDecimal minimumBalance);
    BigDecimal getTotalInterestPaid(Date fromDate, Date toDate);
    List<Account> getAccountsEligibleForInterest();
}