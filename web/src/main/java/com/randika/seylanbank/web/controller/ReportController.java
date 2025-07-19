package com.randika.seylanbank.web.controller;

import com.randika.seylanbank.core.service.*;
import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.model.Transaction;
import com.randika.seylanbank.core.model.Customer;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@WebServlet("/controller/reports/*")
public class ReportController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ReportController.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyy-MM");

    @EJB
    private CustomerReportService customerReportService;

    @EJB
    private ReportGenerationService reportGenerationService;

    @EJB
    private MonthlyReportService monthlyReportService;

    @EJB
    private TransactionReportService transactionReportService;

    @EJB
    private AccountService accountService;

    @EJB
    private TransactionService transactionService;

    @EJB
    private CustomerService customerService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getPathInfo();

        try {
            if (action == null || action.equals("/")) {
                showReportDashboard(request, response);
            } else if (action.equals("/daily-balance")) {
                generateDailyBalanceReport(request, response);
            } else if (action.equals("/monthly-statement")) {
                generateMonthlyStatement(request, response);
            } else if (action.equals("/transaction-report")) {
                generateTransactionReport(request, response);
            } else if (action.equals("/customer-report")) {
                generateCustomerReport(request, response);
            } else if (action.equals("/account-summary")) {
                generateAccountSummaryReport(request, response);
            } else if (action.equals("/interest-report")) {
                generateInterestReport(request, response);
            } else if (action.equals("/download")) {
                downloadReport(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error in ReportController: " + e.getMessage());
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getPathInfo();

        try {
            if (action.equals("/generate")) {
                generateCustomReport(request, response);
            } else if (action.equals("/schedule")) {
                scheduleReport(request, response);
            } else if (action.equals("/email")) {
                emailReport(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error in ReportController POST: " + e.getMessage());
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void showReportDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        if ("CUSTOMER".equals(userRole)) {
            Long customerId = (Long) session.getAttribute("customerId");
            List<Account> accounts = accountService.findAccountsByCustomer(customerId);
            request.setAttribute("accounts", accounts);
            request.getRequestDispatcher("/customer/reports.jsp").forward(request, response);
        } else {
            // Admin can see all report options
            request.setAttribute("totalCustomers", customerService.findAllCustomers().size());
            request.setAttribute("totalAccounts", accountService.findAllAccounts().size());
            request.getRequestDispatcher("/admin/reports-dashboard.jsp").forward(request, response);
        }
    }

    private void generateDailyBalanceReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String dateStr = request.getParameter("date");
        Date reportDate = dateStr != null ? DATE_FORMAT.parse(dateStr) : new Date();

        byte[] reportData = reportGenerationService.generateDailyBalanceReport(reportDate);

        sendReportResponse(response, reportData, "daily-balance-" + DATE_FORMAT.format(reportDate) + ".pdf", "application/pdf");
    }

    private void generateMonthlyStatement(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long accountId = Long.parseLong(request.getParameter("accountId"));
        String monthYearStr = request.getParameter("monthYear");
        Date monthYear = MONTH_FORMAT.parse(monthYearStr);

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        if ("CUSTOMER".equals(userRole)) {
            Long customerId = (Long) session.getAttribute("customerId");
            Account account = accountService.findAccountById(accountId);

            if (!account.getCustomer().getId().equals(customerId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }
        }

        byte[] reportData = monthlyReportService.generateMonthlyStatement(accountId, monthYear);

        sendReportResponse(response, reportData, "statement-" + accountId + "-" + monthYearStr + ".pdf", "application/pdf");
    }

    private void generateTransactionReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");
        String format = request.getParameter("format"); // pdf or excel

        Date fromDate = DATE_FORMAT.parse(fromDateStr);
        Date toDate = DATE_FORMAT.parse(toDateStr);

        byte[] reportData = transactionReportService.generateTransactionReport(fromDate, toDate);

        String filename = "transactions-" + fromDateStr + "-to-" + toDateStr;
        String contentType = "pdf".equals(format) ? "application/pdf" : "application/vnd.ms-excel";
        String extension = "pdf".equals(format) ? ".pdf" : ".xlsx";

        sendReportResponse(response, reportData, filename + extension, contentType);
    }

    private void generateCustomerReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        byte[] reportData = customerReportService.generateCustomerReport();

        sendReportResponse(response, reportData, "customer-report-" + DATE_FORMAT.format(new Date()) + ".pdf", "application/pdf");
    }

    private void generateAccountSummaryReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String accountType = request.getParameter("accountType");
        String status = request.getParameter("status");

        List<Account> accounts = accountService.findAllAccounts();

        if (accountType != null && !accountType.isEmpty()) {
            accounts = accounts.stream()
                    .filter(a -> a.getAccountType().name().equals(accountType))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
            accounts = accounts.stream()
                    .filter(a -> a.getStatus().name().equals(status))
                    .collect(java.util.stream.Collectors.toList());
        }

        request.setAttribute("accounts", accounts);
        request.setAttribute("reportDate", new Date());
        request.getRequestDispatcher("/admin/report-account-summary.jsp").forward(request, response);
    }


    private void generateInterestReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");

        Date fromDate = DATE_FORMAT.parse(fromDateStr);
        Date toDate = DATE_FORMAT.parse(toDateStr);

        // Get interest transactions
        List<Transaction> interestTransactions = transactionService.getTransactionsByType(
                com.randika.seylanbank.core.enums.TransactionType.INTEREST_CREDIT, fromDate, toDate);

        request.setAttribute("interestTransactions", interestTransactions);
        request.setAttribute("fromDate", fromDate);
        request.setAttribute("toDate", toDate);
        request.getRequestDispatcher("/admin/report-interest.jsp").forward(request, response);
    }

    private void generateCustomReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String reportType = request.getParameter("reportType");
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");
        String format = request.getParameter("format");

        LOGGER.info("Generating custom report: " + reportType);

        switch (reportType) {
            case "large-transactions":
                generateLargeTransactionReport(request, response);
                break;
            case "inactive-accounts":
                generateInactiveAccountsReport(request, response);
                break;
            case "customer-activity":
                generateCustomerActivityReport(request, response);
                break;
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
    }

    private void generateLargeTransactionReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");
        String minAmountStr = request.getParameter("minAmount");

        Date fromDate = DATE_FORMAT.parse(fromDateStr);
        Date toDate = DATE_FORMAT.parse(toDateStr);
        java.math.BigDecimal minAmount = new java.math.BigDecimal(minAmountStr);

        List<Transaction> largeTransactions = transactionService.getLargeTransactions(minAmount, fromDate, toDate);

        request.setAttribute("transactions", largeTransactions);
        request.setAttribute("minAmount", minAmount);
        request.setAttribute("fromDate", fromDate);
        request.setAttribute("toDate", toDate);
        request.getRequestDispatcher("/admin/report-large-transactions.jsp").forward(request, response);
    }

    private void generateInactiveAccountsReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        List<Account> inactiveAccounts = accountService.findInactiveAccounts();

        request.setAttribute("accounts", inactiveAccounts);
        request.setAttribute("reportDate", new Date());
        request.getRequestDispatcher("/admin/report-inactive-accounts.jsp").forward(request, response);
    }

    private void generateCustomerActivityReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long customerId = Long.parseLong(request.getParameter("customerId"));
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");

        Date fromDate = DATE_FORMAT.parse(fromDateStr);
        Date toDate = DATE_FORMAT.parse(toDateStr);

        Customer customer = customerService.findCustomerById(customerId);
        List<Account> accounts = accountService.findAccountsByCustomer(customerId);

        List<Transaction> allTransactions = new java.util.ArrayList<>();
        for (Account account : accounts) {
            List<Transaction> transactions = transactionService.getTransactionHistory(
                    account.getId(), fromDate, toDate);
            allTransactions.addAll(transactions);
        }

        request.setAttribute("customer", customer);
        request.setAttribute("accounts", accounts);
        request.setAttribute("transactions", allTransactions);
        request.setAttribute("fromDate", fromDate);
        request.setAttribute("toDate", toDate);
        request.getRequestDispatcher("/admin/report-customer-activity.jsp").forward(request, response);
    }

    private void scheduleReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String reportType = request.getParameter("reportType");
        String frequency = request.getParameter("frequency"); // daily, weekly, monthly
        String email = request.getParameter("email");

        // Implementation would involve creating scheduled tasks
        LOGGER.info("Scheduling report: " + reportType + " with frequency: " + frequency + " to: " + email);

        request.setAttribute("successMessage", "Report scheduled successfully");
        showReportDashboard(request, response);
    }

    private void emailReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String reportType = request.getParameter("reportType");
        String email = request.getParameter("email");

        // Implementation would involve sending email with report attachment
        LOGGER.info("Emailing report: " + reportType + " to: " + email);

        request.setAttribute("successMessage", "Report sent successfully to " + email);
        showReportDashboard(request, response);
    }

    private void downloadReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String reportId = request.getParameter("reportId");
        String reportType = request.getParameter("type");

        // Implementation would retrieve stored report from database or file system
        LOGGER.info("Downloading report: " + reportId + " of type: " + reportType);

        // For demo, generate a sample report
        byte[] reportData = "Sample Report Content".getBytes();
        sendReportResponse(response, reportData, "report-" + reportId + ".pdf", "application/pdf");
    }

    private void sendReportResponse(HttpServletResponse response, byte[] reportData,
                                    String filename, String contentType) throws IOException {

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setContentLength(reportData.length);

        try (OutputStream out = response.getOutputStream()) {
            out.write(reportData);
            out.flush();
        }

        LOGGER.info("Report sent: " + filename);
    }
}