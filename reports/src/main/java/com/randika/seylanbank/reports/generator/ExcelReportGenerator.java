package com.randika.seylanbank.reports.generator;

import com.randika.seylanbank.core.model.Transaction;
import com.randika.seylanbank.core.util.DateUtil;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class ExcelReportGenerator {

    private static final Logger LOGGER = Logger.getLogger(ExcelReportGenerator.class.getName());

    public static byte[] generateTransactionReport(List<Transaction> transactions, Date fromDate, Date toDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            LOGGER.info("Generating Excel transaction report");

            // In a real implementation, you would use Apache POI to create Excel files
            StringBuilder content = new StringBuilder();
            content.append("SEYLAN BANK - TRANSACTION REPORT\n");
            content.append("Period: ").append(fromDate).append(" to ").append(toDate).append("\n\n");

            content.append("Transaction ID\tDate\tAccount\tType\tAmount\tStatus\tDescription\n");

            for (Transaction transaction : transactions) {
                content.append(transaction.getTransactionId()).append("\t")
                        .append(transaction.getTransactionDate()).append("\t")
                        .append(transaction.getAccount().getAccountNumber()).append("\t")
                        .append(transaction.getType().getDisplayName()).append("\t")
                        .append(transaction.getAmount()).append("\t")
                        .append(transaction.getStatus().getDisplayName()).append("\t")
                        .append(transaction.getDescription()).append("\n");
            }

            // This is a placeholder - implement with Apache POI for actual Excel generation
            return content.toString().getBytes();

        } catch (Exception e) {
            LOGGER.severe("Error generating Excel transaction report: " + e.getMessage());
            return new byte[0];
        }
    }

    public static byte[] generateAccountSummaryReport(List<com.randika.seylanbank.core.model.Account> accounts) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            LOGGER.info("Generating Excel account summary report");

            StringBuilder content = new StringBuilder();
            content.append("SEYLAN BANK - ACCOUNT SUMMARY REPORT\n");
            content.append("Generated: ").append(new Date()).append("\n\n");

            content.append("Account Number\tCustomer Name\tAccount Type\tStatus\tBalance\tLast Transaction\n");

            for (com.randika.seylanbank.core.model.Account account : accounts) {
                content.append(account.getAccountNumber()).append("\t")
                        .append(account.getCustomer().getFirstName()).append(" ")
                        .append(account.getCustomer().getLastName()).append("\t")
                        .append(account.getAccountType().getDisplayName()).append("\t")
                        .append(account.getStatus().getDisplayName()).append("\t")
                        .append(account.getBalance()).append("\t")
                        .append(account.getLastTransactionDate()).append("\n");
            }

            return content.toString().getBytes();

        } catch (Exception e) {
            LOGGER.severe("Error generating Excel account summary report: " + e.getMessage());
            return new byte[0];
        }
    }
}