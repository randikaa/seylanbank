package com.randika.seylanbank.web.controller;

import com.randika.seylanbank.core.service.CustomerService;
import com.randika.seylanbank.core.service.UserService;
import com.randika.seylanbank.core.service.AccountService;
import com.randika.seylanbank.core.model.Customer;
import com.randika.seylanbank.core.model.User;
import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.enums.UserRole;
import com.randika.seylanbank.core.util.SecurityUtil;
import com.randika.seylanbank.core.util.ValidationUtil;
import com.randika.seylanbank.banking.validator.CustomerValidator;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/controller/customers/*")
public class CustomerController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CustomerController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @EJB
    private CustomerService customerService;

    @EJB
    private UserService userService;

    @EJB
    private AccountService accountService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getPathInfo();

        try {
            if (action == null || action.equals("/")) {
                listCustomers(request, response);
            } else if (action.equals("/create")) {
                showCreateForm(request, response);
            } else if (action.equals("/edit")) {
                showEditForm(request, response);
            } else if (action.equals("/view")) {
                viewCustomer(request, response);
            } else if (action.equals("/search")) {
                searchCustomers(request, response);
            } else if (action.equals("/profile")) {
                viewCustomerProfile(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error in CustomerController GET: " + e.getMessage());
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
                createCustomer(request, response);
            } else if (action.equals("/update")) {
                updateCustomer(request, response);
            } else if (action.equals("/delete")) {
                deleteCustomer(request, response);
            } else if (action.equals("/updateProfile")) {
                updateCustomerProfile(request, response);
            } else if (action.equals("/changePassword")) {
                changeCustomerPassword(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.severe("Error in CustomerController POST: " + e.getMessage());
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void listCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Customer> customers = customerService.findAllCustomers();
        request.setAttribute("customers", customers);
        request.getRequestDispatcher("/admin/customer-list.jsp").forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/admin/customer-create.jsp").forward(request, response);
    }

    private void createCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get customer data from form
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String phoneNumber = request.getParameter("phoneNumber");
        String nationalId = request.getParameter("nationalId");
        String address = request.getParameter("address");
        String dateOfBirthStr = request.getParameter("dateOfBirth");

        // Validate input
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!ValidationUtil.isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        if (!ValidationUtil.isValidNationalId(nationalId)) {
            throw new IllegalArgumentException("Invalid national ID format");
        }

        // Parse date of birth
        LocalDate dateOfBirth = LocalDate.parse(dateOfBirthStr, DATE_FORMATTER);

        // Create customer
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        customer.setNationalId(nationalId);
        customer.setAddress(address);
        customer.setDateOfBirth(dateOfBirth);
        customer.setCreatedDate(LocalDate.now());

        // Validate customer
        CustomerValidator.validateCustomer(customer);

        Customer savedCustomer = customerService.createCustomer(customer);

        // Create user account if requested
        String createUserAccount = request.getParameter("createUserAccount");
        if ("true".equals(createUserAccount)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (!SecurityUtil.isValidPassword(password)) {
                throw new IllegalArgumentException("Password does not meet security requirements");
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(SecurityUtil.hashPassword(password));
            user.setRole(UserRole.CUSTOMER);
            user.setActive(true);
            user.setCustomer(savedCustomer);

            userService.createUser(user);
        }

        LOGGER.info("Created customer: " + savedCustomer.getId());

        response.sendRedirect(request.getContextPath() + "/controller/customers/view?id=" + savedCustomer.getId());
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long customerId = Long.parseLong(request.getParameter("id"));
        Customer customer = customerService.findCustomerById(customerId);

        request.setAttribute("customer", customer);
        request.getRequestDispatcher("/admin/customer-edit.jsp").forward(request, response);
    }

    private void updateCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long customerId = Long.parseLong(request.getParameter("id"));
        Customer customer = customerService.findCustomerById(customerId);

        // Update customer fields
        customer.setFirstName(request.getParameter("firstName"));
        customer.setLastName(request.getParameter("lastName"));
        customer.setEmail(request.getParameter("email"));
        customer.setPhoneNumber(request.getParameter("phoneNumber"));
        customer.setAddress(request.getParameter("address"));

        // Validate updated customer
        CustomerValidator.validateCustomer(customer);

        customerService.updateCustomer(customer);

        LOGGER.info("Updated customer: " + customerId);

        response.sendRedirect(request.getContextPath() + "/controller/customers/view?id=" + customerId);
    }

    private void viewCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long customerId = Long.parseLong(request.getParameter("id"));
        Customer customer = customerService.findCustomerById(customerId);

        // Get customer's accounts
        List<Account> accounts = accountService.findAccountsByCustomer(customerId);

        request.setAttribute("customer", customer);
        request.setAttribute("accounts", accounts);
        request.getRequestDispatcher("/admin/customer-details.jsp").forward(request, response);
    }

    private void deleteCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long customerId = Long.parseLong(request.getParameter("id"));

        // Check if customer has any active accounts
        List<Account> accounts = accountService.findAccountsByCustomer(customerId);
        if (!accounts.isEmpty()) {
            throw new IllegalStateException("Cannot delete customer with active accounts");
        }

        customerService.deleteCustomer(customerId);

        LOGGER.info("Deleted customer: " + customerId);

        response.sendRedirect(request.getContextPath() + "/controller/customers/");
    }

    private void searchCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String searchTerm = request.getParameter("search");
        String searchType = request.getParameter("type");

        List<Customer> customers = null;

        if ("name".equals(searchType)) {
            customers = customerService.searchByName(searchTerm);
        } else if ("nationalId".equals(searchType)) {
            customers = customerService.searchByNationalId(searchTerm);
        } else if ("email".equals(searchType)) {
            customers = customerService.searchByEmail(searchTerm);
        } else if ("phone".equals(searchType)) {
            customers = customerService.searchByPhoneNumber(searchTerm);
        }

        request.setAttribute("customers", customers);
        request.setAttribute("searchTerm", searchTerm);
        request.getRequestDispatcher("/admin/customer-list.jsp").forward(request, response);
    }

    private void viewCustomerProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Long customerId = (Long) session.getAttribute("customerId");

        if (customerId == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Customer customer = customerService.findCustomerById(customerId);
        List<Account> accounts = accountService.findAccountsByCustomer(customerId);

        request.setAttribute("customer", customer);
        request.setAttribute("accounts", accounts);
        request.getRequestDispatcher("/customer/profile.jsp").forward(request, response);
    }

    private void updateCustomerProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Long customerId = (Long) session.getAttribute("customerId");

        if (customerId == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Customer customer = customerService.findCustomerById(customerId);

        // Update allowed fields only
        customer.setEmail(request.getParameter("email"));
        customer.setPhoneNumber(request.getParameter("phoneNumber"));
        customer.setAddress(request.getParameter("address"));

        CustomerValidator.validateCustomer(customer);

        customerService.updateCustomer(customer);

        LOGGER.info("Customer updated their profile: " + customerId);

        request.setAttribute("successMessage", "Profile updated successfully");
        viewCustomerProfile(request, response);
    }

    private void changeCustomerPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        if (!SecurityUtil.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("New password does not meet security requirements");
        }

        // Verify current password
        User user = userService.authenticateUser((String)session.getAttribute("username"), currentPassword);
        if (user == null) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Change password
        userService.changePassword(userId, SecurityUtil.hashPassword(newPassword));

        LOGGER.info("Customer changed password: " + session.getAttribute("username"));

        request.setAttribute("successMessage", "Password changed successfully");
        viewCustomerProfile(request, response);
    }
}