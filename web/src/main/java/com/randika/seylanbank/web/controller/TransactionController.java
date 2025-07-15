package com.randika.seylanbank.web.controller;

import com.randika.seylanbank.core.service.TransactionService;
import com.randika.seylanbank.core.service.AccountService;
import com.randika.seylanbank.core.model.Transaction;
import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.enums.TransactionType;
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

@WebServlet("/controller/transactions/*")
public class TransactionController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TransactionController.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @EJB
    private TransactionService transactionService;

    @EJB
    private AccountService accountService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getPathInfo();

        try {
            if (action == null || action.equals("/")) {
                listTransactions(request, response);
            } else if (action.equals("/deposit")) {
                showDepositForm(request, response);
            } else if (action.equals("/withdrawal")) {
                showWithdrawalForm(request, response);
            } else if (action.equals("/transfer")) {
                showTransferForm(request, response);
            } else if (action.equals("/history")) {
                showTransactionHistory(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error in TransactionController: " + e.getMessage());
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getPathInfo();

        try {
            if (action.equals("/deposit")) {
                processDeposit(request, response);
            } else if (action.equals("/withdrawal")) {
                processWithdrawal(request, response);
            } else if (action.equals("/transfer")) {
                processTransfer(request, response);
            } else if (action.equals("/reverse")) {
                reverseTransaction(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error in TransactionController: " + e.getMessage());
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void listTransactions(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");

        Date fromDate = fromDateStr != null ? DATE_FORMAT.parse(fromDateStr) :
                new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000); // 30 days ago
        Date toDate = toDateStr != null ? DATE_FORMAT.parse(toDateStr) : new Date();

        List<Transaction> transactions = transactionService.getAllTransactions(fromDate, toDate);

        request.setAttribute("transactions", transactions);
        request.setAttribute("fromDate", fromDateStr);
        request.setAttribute("toDate", toDateStr);
        request.getRequestDispatcher("/admin/transaction-reports.jsp").forward(request, response);
    }

    private void showDepositForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Account> accounts = accountService.findAllAccounts();
        request.setAttribute("accounts", accounts);
        request.getRequestDispatcher("/admin/transaction-deposit.jsp").forward(request, response);
    }

    private void processDeposit(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long accountId = Long.parseLong(request.getParameter("accountId"));
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));

        transactionService.processTransaction(accountId, TransactionType.DEPOSIT, amount);

        LOGGER.info("Deposit processed: " + amount + " to account " + accountId);

        request.setAttribute("successMessage", "Deposit processed successfully");
        showDepositForm(request, response);
    }

    private void showWithdrawalForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Account> accounts = accountService.findAllAccounts();
        request.setAttribute("accounts", accounts);
        request.getRequestDispatcher("/admin/transaction-withdrawal.jsp").forward(request, response);
    }

    private void processWithdrawal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long accountId = Long.parseLong(request.getParameter("accountId"));
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));

        transactionService.processTransaction(accountId, TransactionType.WITHDRAWAL, amount);

        LOGGER.info("Withdrawal processed: " + amount + " from account " + accountId);

        request.setAttribute("successMessage", "Withdrawal processed successfully");
        showWithdrawalForm(request, response);
    }

    private void showTransferForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        if ("CUSTOMER".equals(userRole)) {
            Long customerId = (Long) session.getAttribute("customerId");
            List<Account> accounts = accountService.findAccountsByCustomer(customerId);
            request.setAttribute("accounts", accounts);
            request.getRequestDispatcher("/customer/fund-transfer.jsp").forward(request, response);
        } else {
            List<Account> accounts = accountService.findAllAccounts();
            request.setAttribute("accounts", accounts);
            request.getRequestDispatcher("/admin/transaction-transfer.jsp").forward(request, response);
        }
    }

    private void processTransfer(HttpServletRequest request, HttpServletResponse response)
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

        LOGGER.info("Transfer processed: " + amount + " from account " + fromAccountId + " to " + toAccountId);

        request.setAttribute("successMessage", "Transfer processed successfully");
        showTransferForm(request, response);
    }

    private void showTransactionHistory(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long accountId = Long.parseLong(request.getParameter("accountId"));
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");

        Date fromDate = fromDateStr != null ? DATE_FORMAT.parse(fromDateStr) :
                new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
        Date toDate = toDateStr != null ? DATE_FORMAT.parse(toDateStr) : new Date();

        List<Transaction> transactions = transactionService.getTransactionHistory(accountId, fromDate, toDate);
        Account account = accountService.findAccountById(accountId);

        request.setAttribute("transactions", transactions);
        request.setAttribute("account", account);
        request.setAttribute("fromDate", fromDateStr);
        request.setAttribute("toDate", toDateStr);

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        if ("CUSTOMER".equals(userRole)) {
            request.getRequestDispatcher("/customer/transaction-history.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/admin/transaction-history.jsp").forward(request, response);
        }
    }

    private void reverseTransaction(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Long transactionId = Long.parseLong(request.getParameter("transactionId"));

        transactionService.reverseTransaction(transactionId);

        LOGGER.info("Transaction reversed: " + transactionId);

        request.setAttribute("successMessage", "Transaction reversed successfully");
        listTransactions(request, response);
    }
}