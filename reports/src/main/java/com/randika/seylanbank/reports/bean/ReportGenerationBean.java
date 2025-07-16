package com.randika.seylanbank.reports.bean;

import com.randika.seylanbank.core.service.ReportGenerationService;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.annotation.security.RolesAllowed;

import com.randika.seylanbank.core.model.*;
import com.randika.seylanbank.core.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.io.ByteArrayOutputStream;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ReportGenerationBean implements ReportGenerationService {

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public byte[] generateDailyBalanceReport(Date reportDate) {
        // Implementation for daily balance report
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.status = :status", Account.class);
        query.setParameter("status", AccountStatus.ACTIVE);

        List<Account> accounts = query.getResultList();

        // Generate PDF report (implementation would use iText or similar)
        return generatePDFReport("Daily Balance Report", accounts);
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "CUSTOMER"})
    public byte[] generateMonthlyStatement(Long accountId, Date monthYear) {
        // Implementation for monthly statement
        Account account = em.find(Account.class, accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        // Get transactions for the month
        TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.account.id = :accountId " +
                        "AND YEAR(t.transactionDate) = YEAR(:monthYear) " +
                        "AND MONTH(t.transactionDate) = MONTH(:monthYear) " +
                        "ORDER BY t.transactionDate", Transaction.class);
        query.setParameter("accountId", accountId);
        query.setParameter("monthYear", monthYear);

        List<Transaction> transactions = query.getResultList();

        return generateAccountStatement(account, transactions);
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public byte[] generateTransactionReport(Date fromDate, Date toDate) {
        TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :fromDate AND :toDate " +
                        "ORDER BY t.transactionDate DESC", Transaction.class);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        List<Transaction> transactions = query.getResultList();

        return generateTransactionSummaryReport(transactions, fromDate, toDate);
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public byte[] generateCustomerReport() {
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c ORDER BY c.createdDate DESC", Customer.class);

        List<Customer> customers = query.getResultList();

        return generateCustomerSummaryReport(customers);
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "CUSTOMER"})
    @Override
    public byte[] generateAccountSummaryReport(Long customerId) {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.customer.id = :customerId", Account.class);
        query.setParameter("customerId", customerId);

        List<Account> accounts = query.getResultList();

        return generateCustomerAccountSummary(accounts);
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @Override
    public byte[] generateInterestReport(Date fromDate, Date toDate) {
        TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.type = :interestType " +
                        "AND t.transactionDate BETWEEN :fromDate AND :toDate " +
                        "ORDER BY t.transactionDate DESC", Transaction.class);
        query.setParameter("interestType", TransactionType.INTEREST_CREDIT);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        List<Transaction> interestTransactions = query.getResultList();

        return generateInterestSummaryReport(interestTransactions, fromDate, toDate);
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @Override
    public byte[] generateLargeTransactionReport(BigDecimal minimumAmount, Date fromDate, Date toDate) {
        TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.amount >= :minimumAmount " +
                        "AND t.transactionDate BETWEEN :fromDate AND :toDate " +
                        "ORDER BY t.amount DESC", Transaction.class);
        query.setParameter("minimumAmount", minimumAmount);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        List<Transaction> largeTransactions = query.getResultList();

        return generateLargeTransactionSummary(largeTransactions, minimumAmount);
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @Override
    public byte[] generateInactiveAccountsReport() {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.status = :status " +
                        "OR a.lastTransactionDate < :cutoffDate", Account.class);
        query.setParameter("status", AccountStatus.INACTIVE);
        query.setParameter("cutoffDate", LocalDateTime.now().minusDays(90));

        List<Account> inactiveAccounts = query.getResultList();

        return generateInactiveAccountsSummary(inactiveAccounts);
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @Override
    public byte[] generateUserActivityReport(Date fromDate, Date toDate) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.lastLogin BETWEEN :fromDate AND :toDate " +
                        "ORDER BY u.lastLogin DESC", User.class);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        List<User> activeUsers = query.getResultList();

        return generateUserActivitySummary(activeUsers, fromDate, toDate);
    }

    // Private helper methods for report generation
    private byte[] generatePDFReport(String title, List<Account> accounts) {
        // Implementation would use iText PDF library
        // This is a simplified placeholder
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // PDF generation logic here
            String content = "Report: " + title + "\n";
            content += "Generated on: " + new Date() + "\n\n";

            for (Account account : accounts) {
                content += "Account: " + account.getAccountNumber() +
                        " - Balance: " + account.getBalance() + "\n";
            }

            baos.write(content.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating PDF report: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    private byte[] generateAccountStatement(Account account, List<Transaction> transactions) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            String content = "Account Statement\n";
            content += "Account Number: " + account.getAccountNumber() + "\n";
            content += "Account Type: " + account.getAccountType().getDisplayName() + "\n";
            content += "Current Balance: " + account.getBalance() + "\n\n";
            content += "Transactions:\n";

            for (Transaction transaction : transactions) {
                content += transaction.getTransactionDate() + " - " +
                        transaction.getType().getDisplayName() + " - " +
                        transaction.getAmount() + " - Balance: " +
                        transaction.getBalanceAfter() + "\n";
            }

            baos.write(content.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating account statement: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    private byte[] generateTransactionSummaryReport(List<Transaction> transactions, Date fromDate, Date toDate) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            String content = "Transaction Summary Report\n";
            content += "Period: " + fromDate + " to " + toDate + "\n";
            content += "Total Transactions: " + transactions.size() + "\n\n";

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (Transaction transaction : transactions) {
                content += transaction.getTransactionId() + " - " +
                        transaction.getType().getDisplayName() + " - " +
                        transaction.getAmount() + "\n";
                totalAmount = totalAmount.add(transaction.getAmount());
            }

            content += "\nTotal Amount: " + totalAmount + "\n";

            baos.write(content.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating transaction summary: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    private byte[] generateCustomerSummaryReport(List<Customer> customers) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            String content = "Customer Summary Report\n";
            content += "Total Customers: " + customers.size() + "\n\n";

            for (Customer customer : customers) {
                content += customer.getCustomerNumber() + " - " +
                        customer.getFullName() + " - " +
                        customer.getCreatedDate() + "\n";
            }

            baos.write(content.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating customer summary: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    private byte[] generateCustomerAccountSummary(List<Account> accounts) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            String content = "Account Summary\n";
            content += "Total Accounts: " + accounts.size() + "\n\n";

            BigDecimal totalBalance = BigDecimal.ZERO;
            for (Account account : accounts) {
                content += account.getAccountNumber() + " - " +
                        account.getAccountType().getDisplayName() + " - " +
                        account.getBalance() + "\n";
                totalBalance = totalBalance.add(account.getBalance());
            }

            content += "\nTotal Balance: " + totalBalance + "\n";

            baos.write(content.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating account summary: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    private byte[] generateInterestSummaryReport(List<Transaction> interestTransactions, Date fromDate, Date toDate) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            String content = "Interest Summary Report\n";
            content += "Period: " + fromDate + " to " + toDate + "\n";
            content += "Total Interest Transactions: " + interestTransactions.size() + "\n\n";

            BigDecimal totalInterest = BigDecimal.ZERO;
            for (Transaction transaction : interestTransactions) {
                content += transaction.getAccount().getAccountNumber() + " - " +
                        transaction.getAmount() + " - " +
                        transaction.getTransactionDate() + "\n";
                totalInterest = totalInterest.add(transaction.getAmount());
            }

            content += "\nTotal Interest Paid: " + totalInterest + "\n";

            baos.write(content.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating interest summary: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    private byte[] generateLargeTransactionSummary(List<Transaction> largeTransactions, BigDecimal minimumAmount) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            String content = "Large Transaction Report\n";
            content += "Minimum Amount: " + minimumAmount + "\n";
            content += "Total Large Transactions: " + largeTransactions.size() + "\n\n";

            for (Transaction transaction : largeTransactions) {
                content += transaction.getTransactionId() + " - " +
                        transaction.getAccount().getAccountNumber() + " - " +
                        transaction.getAmount() + " - " +
                        transaction.getTransactionDate() + "\n";
            }

            baos.write(content.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating large transaction summary: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    private byte[] generateInactiveAccountsSummary(List<Account> inactiveAccounts) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            String content = "Inactive Accounts Report\n";
            content += "Total Inactive Accounts: " + inactiveAccounts.size() + "\n\n";

            for (Account account : inactiveAccounts) {
                content += account.getAccountNumber() + " - " +
                        account.getCustomer().getFullName() + " - " +
                        account.getStatus() + " - " +
                        account.getLastTransactionDate() + "\n";
            }

            baos.write(content.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating inactive accounts summary: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    private byte[] generateUserActivitySummary(List<User> activeUsers, Date fromDate, Date toDate) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            String content = "User Activity Report\n";
            content += "Period: " + fromDate + " to " + toDate + "\n";
            content += "Active Users: " + activeUsers.size() + "\n\n";

            for (User user : activeUsers) {
                content += user.getUsername() + " - " +
                        user.getRole().getDisplayName() + " - " +
                        "Last Login: " + user.getLastLogin() + "\n";
            }

            baos.write(content.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating user activity summary: " + e.getMessage());
        }

        return baos.toByteArray();
    }
}