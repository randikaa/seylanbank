package com.randika.seylanbank.reports.bean;

import com.randika.seylanbank.core.service.MonthlyReportService;
import com.randika.seylanbank.core.service.AccountService;
import com.randika.seylanbank.core.service.TransactionService;
import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.model.Transaction;
import com.randika.seylanbank.reports.generator.PDFReportGenerator;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class MonthlyReportBean implements MonthlyReportService {

    private static final Logger LOGGER = Logger.getLogger(MonthlyReportBean.class.getName());

    @EJB
    private AccountService accountService;

    @EJB
    private TransactionService transactionService;

    @Override
    @RolesAllowed({"ADMIN", "CUSTOMER"})
    public byte[] generateMonthlyStatement(Long accountId, Date monthYear) {
        try {
            LOGGER.info("Generating monthly statement for account: " + accountId);

            Account account = accountService.findAccountById(accountId);

            // Calculate date range for the month
            LocalDate monthStart = monthYear.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    .withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            Date startDate = Date.from(monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(monthEnd.atStartOfDay(ZoneId.systemDefault()).toInstant());

            List<Transaction> transactions = transactionService.getTransactionHistory(accountId, startDate, endDate);

            // Generate PDF report
            return PDFReportGenerator.generateMonthlyStatement(account, transactions, monthStart);

        } catch (Exception e) {
            LOGGER.severe("Error generating monthly statement: " + e.getMessage());
            return new byte[0];
        }
    }
}