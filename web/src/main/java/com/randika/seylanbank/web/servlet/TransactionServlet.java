package com.randika.seylanbank.web.servlet;

import com.randika.seylanbank.core.service.AccountService;
import com.randika.seylanbank.core.service.TransactionService;
import com.randika.seylanbank.core.service.ScheduledOperationService;
import com.randika.seylanbank.core.model.Transaction;
import com.randika.seylanbank.core.enums.TransactionType;
import com.randika.seylanbank.banking.validator.TransactionValidator;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/transaction/*")
public class TransactionServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TransactionServlet.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @EJB
    private TransactionService transactionService;

    @EJB
    private ScheduledOperationService scheduledOperationService;

    @EJB
    private AccountService accountService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleTransactionHistory(request, response);
            } else if (pathInfo.startsWith("/transfer")) {
                handleTransferForm(request, response);
            } else if (pathInfo.startsWith("/schedule")) {
                handleScheduleForm(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error handling transaction request: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.startsWith("/process")) {
                handleProcessTransaction(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/transfer")) {
                handleTransferFunds(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/schedule")) {
                handleScheduleTransfer(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/reverse")) {
                handleReverseTransaction(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error processing transaction request: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void handleTransactionHistory(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long accountId = Long.parseLong(request.getParameter("accountId"));
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");

        Date fromDate = DATE_FORMAT.parse(fromDateStr);
        Date toDate = DATE_FORMAT.parse(toDateStr);

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        List<Transaction> transactions;
        if ("CUSTOMER".equals(userRole)) {
            Long customerId = (Long) session.getAttribute("customerId");
            transactions = transactionService.getMyTransactionHistory(accountId, fromDate, toDate, customerId);
        } else {
            transactions = transactionService.getTransactionHistory(accountId, fromDate, toDate);
        }

        request.setAttribute("transactions", transactions);
        request.setAttribute("accountId", accountId);

        if ("CUSTOMER".equals(userRole)) {
            request.getRequestDispatcher("/customer/transaction-history.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/admin/transaction-reports.jsp").forward(request, response);
        }
    }

    private void handleTransferForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Long customerId = (Long) session.getAttribute("customerId");

        // Get customer's accounts for the dropdown
        request.setAttribute("customerAccounts", accountService.findAccountsByCustomer(customerId));
        request.getRequestDispatcher("/customer/fund-transfer.jsp").forward(request, response);
    }

    private void handleScheduleForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Long customerId = (Long) session.getAttribute("customerId");

        request.setAttribute("customerAccounts", accountService.findAccountsByCustomer(customerId));
        request.getRequestDispatcher("/customer/schedule-transfer.jsp").forward(request, response);
    }

    private void handleProcessTransaction(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long accountId = Long.parseLong(request.getParameter("accountId"));
        TransactionType type = TransactionType.valueOf(request.getParameter("transactionType"));
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));

        TransactionValidator.validateAmount(amount);

        transactionService.processTransaction(accountId, type, amount);

        LOGGER.info("Processed transaction: " + type + " for amount: " + amount + " on account: " + accountId);

        response.sendRedirect(request.getContextPath() + "/account/details?id=" + accountId);
    }

    private void handleTransferFunds(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long fromAccountId = Long.parseLong(request.getParameter("fromAccountId"));
        Long toAccountId = Long.parseLong(request.getParameter("toAccountId"));
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        if ("CUSTOMER".equals(userRole)) {
            Long customerId = (Long) session.getAttribute("customerId");
            transactionService.customerTransferFunds(fromAccountId, toAccountId, amount, customerId);
        } else {
            transactionService.transferFunds(fromAccountId, toAccountId, amount);
        }

        LOGGER.info("Transfer completed: " + amount + " from account " + fromAccountId + " to " + toAccountId);

        request.setAttribute("successMessage", "Transfer completed successfully");
        request.getRequestDispatcher("/customer/fund-transfer.jsp").forward(request, response);
    }

    private void handleScheduleTransfer(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long fromAccountId = Long.parseLong(request.getParameter("fromAccountId"));
        Long toAccountId = Long.parseLong(request.getParameter("toAccountId"));
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));
        String scheduledDateStr = request.getParameter("scheduledDate");

        Date scheduledDate = DATE_FORMAT.parse(scheduledDateStr);

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        if ("CUSTOMER".equals(userRole)) {
            Long customerId = (Long) session.getAttribute("customerId");
            scheduledOperationService.customerScheduleTransfer(fromAccountId, toAccountId, amount, scheduledDate, customerId);
        } else {
            scheduledOperationService.scheduleTransfer(fromAccountId, toAccountId, amount, scheduledDate);
        }

        LOGGER.info("Scheduled transfer: " + amount + " from account " + fromAccountId + " to " + toAccountId + " on " + scheduledDate);

        request.setAttribute("successMessage", "Transfer scheduled successfully");
        request.getRequestDispatcher("/customer/schedule-transfer.jsp").forward(request, response);
    }

    private void handleReverseTransaction(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long transactionId = Long.parseLong(request.getParameter("transactionId"));

        transactionService.reverseTransaction(transactionId);

        LOGGER.info("Reversed transaction: " + transactionId);

        response.sendRedirect(request.getContextPath() + "/transaction/");
    }
}