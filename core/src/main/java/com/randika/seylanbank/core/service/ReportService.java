package com.randika.seylanbank.core.service;

import jakarta.ejb.Remote;
import java.util.Date;

@Remote
public interface ReportService {
    byte[] generateDailyBalanceReport(Date reportDate);
    byte[] generateMonthlyStatement(Long accountId, Date monthYear);
    byte[] generateTransactionReport(Date fromDate, Date toDate);
    byte[] generateCustomerReport();

    // Additional report methods
    byte[] generateAccountSummaryReport(Long customerId);
    byte[] generateInterestReport(Date fromDate, Date toDate);
    byte[] generateLargeTransactionReport(java.math.BigDecimal minimumAmount, Date fromDate, Date toDate);
    byte[] generateInactiveAccountsReport();
    byte[] generateUserActivityReport(Date fromDate, Date toDate);
}