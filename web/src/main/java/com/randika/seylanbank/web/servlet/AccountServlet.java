package com.randika.seylanbank.web.servlet;

import com.randika.seylanbank.core.service.AccountService;
import com.randika.seylanbank.core.service.CustomerService;
import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.model.Customer;
import com.randika.seylanbank.core.enums.AccountType;
import com.randika.seylanbank.banking.validator.AccountValidator;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/account/*")
public class AccountServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AccountServlet.class.getName());

    @EJB
    private AccountService accountService;

    @EJB
    private CustomerService customerService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                handleListAccounts(request, response);
            } else if (pathInfo.startsWith("/details")) {
                handleAccountDetails(request, response);
            } else if (pathInfo.startsWith("/create")) {
                handleCreateAccountForm(request, response);
            } else if (pathInfo.startsWith("/balance")) {
                handleAccountBalance(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error handling account request: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.startsWith("/create")) {
                handleCreateAccount(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/update")) {
                handleUpdateAccount(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/close")) {
                handleCloseAccount(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error processing account request: " + e.getMessage());
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void handleListAccounts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        if ("CUSTOMER".equals(userRole)) {
            Long customerId = (Long) session.getAttribute("customerId");
            List<Account> accounts = accountService.findAccountsByCustomer(customerId);
            request.setAttribute("accounts", accounts);
            request.getRequestDispatcher("/customer/account-summary.jsp").forward(request, response);
        } else {
            List<Account> accounts = accountService.findAllAccounts();
            request.setAttribute("accounts", accounts);
            request.getRequestDispatcher("/admin/account-list.jsp").forward(request, response);
        }
    }

    private void handleAccountDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long accountId = Long.parseLong(request.getParameter("id"));
        Account account = accountService.findAccountById(accountId);

        request.setAttribute("account", account);

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        if ("CUSTOMER".equals(userRole)) {
            request.getRequestDispatcher("/customer/account-details.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/admin/account-details.jsp").forward(request, response);
        }
    }

    private void handleCreateAccountForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Customer> customers = customerService.findAllCustomers();
        request.setAttribute("customers", customers);
        request.setAttribute("accountTypes", AccountType.values());
        request.getRequestDispatcher("/admin/account-create.jsp").forward(request, response);
    }

    private void handleCreateAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long customerId = Long.parseLong(request.getParameter("customerId"));
        AccountType accountType = AccountType.valueOf(request.getParameter("accountType"));

        Customer customer = customerService.findCustomerById(customerId);

        AccountValidator.validateAccountCreation(customer, accountType);

        Account account = accountService.createAccount(customer, accountType);

        LOGGER.info("Created new account: " + account.getAccountNumber() + " for customer: " + customerId);

        response.sendRedirect(request.getContextPath() + "/account/details?id=" + account.getId());
    }

    private void handleUpdateAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long accountId = Long.parseLong(request.getParameter("accountId"));
        Account account = accountService.findAccountById(accountId);

        // Update account fields based on request parameters
        // Implementation depends on what fields can be updated

        accountService.updateAccount(account);

        LOGGER.info("Updated account: " + account.getAccountNumber());

        response.sendRedirect(request.getContextPath() + "/account/details?id=" + account.getId());
    }

    private void handleCloseAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long accountId = Long.parseLong(request.getParameter("accountId"));

        accountService.closeAccount(accountId);

        LOGGER.info("Closed account ID: " + accountId);

        response.sendRedirect(request.getContextPath() + "/account/");
    }

    private void handleAccountBalance(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long accountId = Long.parseLong(request.getParameter("id"));
        BigDecimal balance = accountService.getAccountBalance(accountId);

        response.setContentType("application/json");
        response.getWriter().write("{\"balance\": " + balance + "}");
    }
}