package com.randika.seylanbank.reports.generator;

import com.randika.seylanbank.core.model.Transaction;
import com.randika.seylanbank.core.model.Account;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

public class CSVReportGenerator {

    private static final Logger LOGGER = Logger.getLogger(CSVReportGenerator.class.getName());
    private static final String CSV_SEPARATOR = ",";

    public static byte[] generateTransactionCSV(List<Transaction> transactions) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos))) {

            LOGGER.info("Generating CSV transaction report");

            // Header
            writer.println("Transaction ID,Date,Account Number,Type,Amount,Status,Description");

            // Data rows
            for (Transaction transaction : transactions) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                        escapeCSV(transaction.getTransactionId()),
                        transaction.getTransactionDate(),
                        escapeCSV(transaction.getAccount().getAccountNumber()),
                        escapeCSV(transaction.getType().getDisplayName()),
                        transaction.getAmount(),
                        escapeCSV(transaction.getStatus().getDisplayName()),
                        escapeCSV(transaction.getDescription())
                );
            }

            writer.flush();
            return baos.toByteArray();

        } catch (Exception e) {
            LOGGER.severe("Error generating CSV transaction report: " + e.getMessage());
            return new byte[0];
        }
    }

    public static byte[] generateAccountCSV(List<Account> accounts) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos))) {

            LOGGER.info("Generating CSV account report");

            // Header
            writer.println("Account Number,Customer Name,Account Type,Status,Balance,Created Date");

            // Data rows
            for (Account account : accounts) {
                writer.printf("%s,%s,%s,%s,%s,%s%n",
                        escapeCSV(account.getAccountNumber()),
                        escapeCSV(account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName()),
                        escapeCSV(account.getAccountType().getDisplayName()),
                        escapeCSV(account.getStatus().getDisplayName()),
                        account.getBalance(),
                        account.getCreatedDate()
                );
            }

            writer.flush();
            return baos.toByteArray();

        } catch (Exception e) {
            LOGGER.severe("Error generating CSV account report: " + e.getMessage());
            return new byte[0];
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(CSV_SEPARATOR) || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}