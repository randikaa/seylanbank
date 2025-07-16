package com.randika.seylanbank.reports.scheduler;

import com.randika.seylanbank.core.service.ReportGenerationService;
import com.randika.seylanbank.core.service.AccountService;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.util.Date;
import java.util.logging.Logger;

@Singleton
@Startup
public class DailyReportScheduler {

    private static final Logger LOGGER = Logger.getLogger(DailyReportScheduler.class.getName());

    @EJB
    private ReportGenerationService reportService;

    @EJB
    private AccountService accountService;

    @Schedule(hour = "1", minute = "0", second = "0", persistent = false, info = "DailyReport")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void generateDailyBalanceReport() {
        LOGGER.info("Starting daily balance report generation");

        try {
            Date reportDate = new Date();
            byte[] report = reportService.generateDailyBalanceReport(reportDate);

            if (report != null && report.length > 0) {
                // In a real implementation, you would save the report to a file system or send via email
                saveReportToFileSystem(report, "daily_balance_" + reportDate.toString() + ".pdf");
                LOGGER.info("Daily balance report generated successfully");
            } else {
                LOGGER.warning("Daily balance report generation returned empty result");
            }

        } catch (Exception e) {
            LOGGER.severe("Error generating daily balance report: " + e.getMessage());
        }
    }

    @Schedule(hour = "2", minute = "0", second = "0", persistent = false, info = "DailyTransactionSummary")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void generateDailyTransactionSummary() {
        LOGGER.info("Starting daily transaction summary generation");

        try {
            Date today = new Date();
            Date yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);

            byte[] report = reportService.generateTransactionReport(yesterday, today);

            if (report != null && report.length > 0) {
                saveReportToFileSystem(report, "daily_transactions_" + today.toString() + ".xlsx");
                LOGGER.info("Daily transaction summary generated successfully");
            }

        } catch (Exception e) {
            LOGGER.severe("Error generating daily transaction summary: " + e.getMessage());
        }
    }

    @Schedule(hour = "3", minute = "0", second = "0", dayOfWeek = "1", persistent = false, info = "WeeklyReport")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void generateWeeklyReports() {
        LOGGER.info("Starting weekly reports generation");

        try {
            // Generate customer report weekly
            byte[] customerReport = reportService.generateCustomerReport();

            if (customerReport != null && customerReport.length > 0) {
                saveReportToFileSystem(customerReport, "weekly_customer_report_" + new Date().toString() + ".pdf");
                LOGGER.info("Weekly customer report generated successfully");
            }

        } catch (Exception e) {
            LOGGER.severe("Error generating weekly reports: " + e.getMessage());
        }
    }

    private void saveReportToFileSystem(byte[] reportData, String fileName) {
        try {
            // In a real implementation, save to a designated reports directory
            // For now, just log the operation
            LOGGER.info("Saving report: " + fileName + " (" + reportData.length + " bytes)");

            // Example implementation:
            // Path reportsDir = Paths.get("/opt/seylanbank/reports");
            // Files.createDirectories(reportsDir);
            // Files.write(reportsDir.resolve(fileName), reportData);

        } catch (Exception e) {
            LOGGER.severe("Error saving report to file system: " + e.getMessage());
        }
    }
}