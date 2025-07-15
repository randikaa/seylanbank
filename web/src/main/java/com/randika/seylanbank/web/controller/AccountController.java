package com.randika.seylanbank.web.controller;

import com.randika.seylanbank.core.service.AccountService;
import com.randika.seylanbank.core.service.CustomerService;
import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.model.Customer;
import com.randika.seylanbank.core.enums.AccountType;
import com.randika.seylanbank.core.enums.AccountStatus;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/controller/accounts/*")
public class AccountController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AccountController.class.getName());

    @EJB
    private AccountService accountService;

    @EJB
    private CustomerService customerService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getPathInfo();

        try {
            if (action == null || action.equals("/")) {
                listAccounts(request, response);
            } else if (action.equals("/create")) {
                showCreateForm(request, response);
            } else if (action.equals("/edit")) {
                showEditForm(request, response);
            } else if (action.equals("/view")) {
                viewAccount(request, response);
            } else if (action.equals("/search")) {
                searchAccounts(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error in AccountController: " + e.getMessage());
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getPathInfo();

        try {
            if (action.equals("/create")) {
                createAccount(request, response);
            } else if (action.equals("/update")) {
                updateAccount(request, response);
            } else if (action.equals("/close")) {
                closeAccount(request, response);
            } else if (action.equals("/suspend")) {
                suspendAccount(request, response);
            } else if (action.equals("/activate")) {
                activateAccount(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error in AccountController: " + e.getMessage());
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void listAccounts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Account> accounts = accountService.findAllAccounts();
        request.setAttribute("accounts", accounts);
        request.getRequestDispatcher("/admin/account-list.jsp").forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Customer> customers = customerService.findAllCustomers();
        request.setAttribute("customers", customers);
        request.setAttribute("accountTypes", AccountType.values());
        request.getRequestDispatcher("/admin/account-create.jsp").forward(request, response);
    }

    private void createAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long customerId = Long.parseLong(request.getParameter("customerId"));
        AccountType accountType = AccountType.valueOf(request.getParameter("accountType"));

        Customer customer = customerService.findCustomerById(customerId);
        Account account = accountService.createAccount(customer, accountType);

        LOGGER.info("Created account: " + account.getAccountNumber());

        response.sendRedirect(request.getContextPath() + "/controller/accounts/view?id=" + account.getId());
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long accountId = Long.parseLong(request.getParameter("id"));
        Account account = accountService.findAccountById(accountId);

        request.setAttribute("account", account);
        request.setAttribute("accountStatuses", AccountStatus.values());
        request.getRequestDispatcher("/admin/account-edit.jsp").forward(request, response);
    }

    private void updateAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long accountId = Long.parseLong(request.getParameter("id"));
        Account account = accountService.findAccountById(accountId);

        // Update account properties based on form data
        // This is a simplified version - add validation and proper field updates

        accountService.updateAccount(account);

        LOGGER.info("Updated account: " + account.getAccountNumber());

        response.sendRedirect(request.getContextPath() + "/controller/accounts/view?id=" + account.getId());
    }

    private void viewAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long accountId = Long.parseLong(request.getParameter("id"));
        Account account = accountService.findAccountById(accountId);

        request.setAttribute("account", account);
        request.getRequestDispatcher("/admin/account-details.jsp").forward(request, response);
    }

    private void closeAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long accountId = Long.parseLong(request.getParameter("id"));
        accountService.closeAccount(accountId);

        LOGGER.info("Closed account ID: " + accountId);

        response.sendRedirect(request.getContextPath() + "/controller/accounts/");
    }

    private void suspendAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long accountId = Long.parseLong(request.getParameter("id"));
        accountService.suspendAccount(accountId);

        LOGGER.info("Suspended account ID: " + accountId);

        response.sendRedirect(request.getContextPath() + "/controller/accounts/view?id=" + accountId);
    }

    private void activateAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long accountId = Long.parseLong(request.getParameter("id"));
        accountService.activateAccount(accountId);

        LOGGER.info("Activated account ID: " + accountId);

        response.sendRedirect(request.getContextPath() + "/controller/accounts/view?id=" + accountId);
    }

    private void searchAccounts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String searchTerm = request.getParameter("search");
        String searchType = request.getParameter("type");

        List<Account> accounts = null;

        if ("accountNumber".equals(searchType)) {
            // Search by account number
            accounts = accountService.findByAccountNumber(searchTerm);
        } else if ("customerName".equals(searchType)) {
            // Search by customer name
            accounts = accountService.findByCustomerName(searchTerm);
        } else if ("accountType".equals(searchType)) {
            AccountType type = AccountType.valueOf(searchTerm);
            accounts = accountService.findAccountsByType(type);
        }

        request.setAttribute("accounts", accounts);
        request.setAttribute("searchTerm", searchTerm);
        request.getRequestDispatcher("/admin/account-list.jsp").forward(request, response);
    }
}