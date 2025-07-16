package com.randika.seylanbank.core.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Remote;

import java.math.BigDecimal;
import java.util.Date;

@Remote
public interface ReportGenerationService {
    byte[] generateDailyBalanceReport(Date reportDate);
    byte[] generateMonthlyStatement(Long accountId, Date monthYear);
    byte[] generateTransactionReport(Date fromDate, Date toDate);
    byte[] generateCustomerReport();

    byte[] generateAccountSummaryReport(Long customerId);

    byte[] generateInterestReport(Date fromDate, Date toDate);

    byte[] generateLargeTransactionReport(BigDecimal minimumAmount, Date fromDate, Date toDate);

    byte[] generateInactiveAccountsReport();

    byte[] generateUserActivityReport(Date fromDate, Date toDate);
}
