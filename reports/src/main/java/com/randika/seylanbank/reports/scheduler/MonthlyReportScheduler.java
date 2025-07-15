package com.randika.seylanbank.reports.scheduler;

import com.randika.seylanbank.core.service.ReportService;
import com.randika.seylanbank.core.service.AccountService;
import com.randika.seylanbank.core.service.CustomerService;
import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.model.Customer;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Singleton
@Startup
public class MonthlyReportScheduler {

    private static final Logger LOGGER = Logger.getLogger(MonthlyReportScheduler.class.getName());

    @EJB
    private ReportService reportService;

    @EJB
    private AccountService accountService;

    @EJB
    private CustomerService customerService;

    @Schedule(dayOfMonth = "1", hour = "2", minute = "0", second = "0", persistent = false, info = "MonthlyStatements")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void generateMonthlyStatements() {
        LOGGER.info("Starting monthly statements generation");

        try {
            // Get previous month
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            Date previousMonth = cal.getTime();

            List<Customer> customers = customerService.findAllCustomers();

            for (Customer customer : customers) {
                List<Account> customerAccounts = accountService.findAccountsByCustomer(customer.getId());

                for (Account account : customerAccounts) {
                    try {
                        byte[] statement = reportService.generateMonthlyStatement(account.getId(), previousMonth);

                        if (statement != null && statement.length > 0) {
                            String fileName = "monthly_statement_" + account.getAccountNumber() +
                                    "_" + cal.get(Calendar.YEAR) + "_" + (cal.get(Calendar.MONTH) + 1) + ".pdf";
                            saveStatement(statement, fileName, customer);
                        }

                    } catch (Exception e) {
                        LOGGER.warning("Failed to generate statement for account " + account.getAccountNumber() +
                                ": " + e.getMessage());
                    }
                }
            }

            LOGGER.info("Monthly statements generation completed");

        } catch (Exception e) {
            LOGGER.severe("Error during monthly statements generation: " + e.getMessage());
        }
    }

    @Schedule(dayOfMonth = "15", hour = "1", minute = "0", second = "0", persistent = false, info = "MonthlyAnalytics")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void generateMonthlyAnalytics() {
        LOGGER.info("Starting monthly analytics generation");

        try {
            // Get date range for current month so far
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date monthStart = cal.getTime();
            Date now = new Date();

            byte[] analyticsReport = reportService.generateTransactionReport(monthStart, now);

            if (analyticsReport != null && analyticsReport.length > 0) {
                String fileName = "monthly_analytics_" + cal.get(Calendar.YEAR) + "_" +
                        (cal.get(Calendar.MONTH) + 1) + ".xlsx";
                saveReportToFileSystem(analyticsReport, fileName);
                LOGGER.info("Monthly analytics report generated successfully");
            }

        } catch (Exception e) {
            LOGGER.severe("Error generating monthly analytics: " + e.getMessage());
        }
    }

    private void saveStatement(byte[] statementData, String fileName, Customer customer) {
        try {
            // In a real implementation, you would:
            // 1. Save to file system
            // 2. Send via email to customer
            // 3. Store in document management system

            LOGGER.info("Saving monthly statement for customer: " + customer.getEmail() +
                    ", File: " + fileName + " (" + statementData.length + " bytes)");

            // Example: Send email notification
            // emailService.sendMonthlyStatement(customer.getEmail(), statementData, fileName);

        } catch (Exception e) {
            LOGGER.severe("Error saving monthly statement: " + e.getMessage());
        }
    }

    private void saveReportToFileSystem(byte[] reportData, String fileName) {
        try {
            LOGGER.info("Saving monthly report: " + fileName + " (" + reportData.length + " bytes)");

            // In a real implementation, save to a designated reports directory
            // Path reportsDir = Paths.get("/opt/seylanbank/reports/monthly");
            // Files.createDirectories(reportsDir);
            // Files.write(reportsDir.resolve(fileName), reportData);

        } catch (Exception e) {
            LOGGER.severe("Error saving monthly report to file system: " + e.getMessage());
        }
    }
}