package com.randika.seylanbank.reports.generator;

import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.model.Customer;
import com.randika.seylanbank.core.model.Transaction;
import com.randika.seylanbank.core.util.DateUtil;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class PDFReportGenerator {

    private static final Logger LOGGER = Logger.getLogger(PDFReportGenerator.class.getName());

    public static byte[] generateMonthlyStatement(Account account, List<Transaction> transactions, LocalDate month) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            LOGGER.info("Generating PDF monthly statement for account: " + account.getAccountNumber());

            // In a real implementation, you would use a PDF library like iText
            StringBuilder content = new StringBuilder();
            content.append("SEYLAN BANK - MONTHLY STATEMENT\n\n");
            content.append("Account Number: ").append(account.getAccountNumber()).append("\n");
            content.append("Account Holder: ").append(account.getCustomer().getFirstName())
                    .append(" ").append(account.getCustomer().getLastName()).append("\n");
            content.append("Statement Period: ").append(DateUtil.formatDateForDisplay(month)).append("\n");
            content.append("Current Balance: $").append(account.getBalance()).append("\n\n");

            content.append("TRANSACTION HISTORY\n");
            content.append("Date\t\tType\t\tAmount\t\tBalance\n");
            content.append("------------------------------------------------\n");

            for (Transaction transaction : transactions) {
                content.append(DateUtil.formatDateForDisplay(
                                transaction.getTransactionDate()
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDate()))
                        .append("\t").append(transaction.getType().getDisplayName())
                        .append("\t$").append(transaction.getAmount())
                        .append("\t$").append(transaction.getBalanceAfter())
                        .append("\n");
            }

            // Convert to PDF (this is a placeholder - implement with actual PDF library)
            return content.toString().getBytes();

        } catch (Exception e) {
            LOGGER.severe("Error generating PDF monthly statement: " + e.getMessage());
            return new byte[0];
        }
    }

    public static byte[] generateCustomerReport(List<Customer> customers) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            LOGGER.info("Generating PDF customer report");

            StringBuilder content = new StringBuilder();
            content.append("SEYLAN BANK - CUSTOMER REPORT\n\n");
            content.append("Generated: ").append(DateUtil.formatDateTimeForDisplay(java.time.LocalDateTime.now())).append("\n\n");
            content.append("Total Customers: ").append(customers.size()).append("\n\n");

            content.append("CUSTOMER LIST\n");
            content.append("Name\t\tEmail\t\tPhone\t\tJoin Date\n");
            content.append("--------------------------------------------------------\n");

            for (Customer customer : customers) {
                content.append(customer.getFirstName()).append(" ").append(customer.getLastName())
                        .append("\t").append(customer.getEmail())
                        .append("\t").append(customer.getPhoneNumber())
                        .append("\t").append(DateUtil.formatDateForDisplay(customer.getCreatedDate().toLocalDate()))
                        .append("\n");
            }

            return content.toString().getBytes();

        } catch (Exception e) {
            LOGGER.severe("Error generating PDF customer report: " + e.getMessage());
            return new byte[0];
        }
    }

    public static byte[] generateDailyBalanceReport(List<Account> accounts, LocalDate reportDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            LOGGER.info("Generating PDF daily balance report for: " + reportDate);

            StringBuilder content = new StringBuilder();
            content.append("SEYLAN BANK - DAILY BALANCE REPORT\n\n");
            content.append("Report Date: ").append(DateUtil.formatDateForDisplay(reportDate)).append("\n\n");

            content.append("ACCOUNT BALANCES\n");
            content.append("Account Number\t\tCustomer\t\tType\t\tBalance\n");
            content.append("--------------------------------------------------------\n");

            java.math.BigDecimal totalBalance = java.math.BigDecimal.ZERO;
            for (Account account : accounts) {
                content.append(account.getAccountNumber())
                        .append("\t").append(account.getCustomer().getFirstName())
                        .append(" ").append(account.getCustomer().getLastName())
                        .append("\t").append(account.getAccountType().getDisplayName())
                        .append("\t$").append(account.getBalance())
                        .append("\n");
                totalBalance = totalBalance.add(account.getBalance());
            }

            content.append("\nTotal Bank Balance: $").append(totalBalance).append("\n");

            return content.toString().getBytes();

        } catch (Exception e) {
            LOGGER.severe("Error generating PDF daily balance report: " + e.getMessage());
            return new byte[0];
        }
    }
}
