package com.randika.seylanbank.web.servlet;

import com.randika.seylanbank.core.service.UserService;
import com.randika.seylanbank.core.service.CustomerService;
import com.randika.seylanbank.core.model.User;
import com.randika.seylanbank.core.model.Customer;
import com.randika.seylanbank.core.enums.UserRole;
import com.randika.seylanbank.core.util.SecurityUtil;
import com.randika.seylanbank.core.util.ValidationUtil;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());

    @EJB
    private UserService userService;

    @EJB
    private CustomerService customerService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Get registration data
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");
            String phoneNumber = request.getParameter("phoneNumber");
            String nationalId = request.getParameter("nationalId");
            String address = request.getParameter("address");
            String dateOfBirthStr = request.getParameter("dateOfBirth");

            // Validate input
            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Passwords do not match");
            }

            if (!SecurityUtil.isValidPassword(password)) {
                throw new IllegalArgumentException("Password does not meet security requirements");
            }

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
            LocalDate dateOfBirth = LocalDate.parse(dateOfBirthStr, DateTimeFormatter.ISO_LOCAL_DATE);

            // Create customer
            Customer customer = new Customer();
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            customer.setEmail(email);
            customer.setPhoneNumber(phoneNumber);
            customer.setNationalId(nationalId);
            customer.setAddress(address);
            customer.setDateOfBirth(dateOfBirth);
            customer.setCreatedDate(LocalDateTime.now());

            Customer savedCustomer = customerService.createCustomer(customer);

            // Create user
            User user = new User();
            user.setUsername(username);
            user.setPassword(SecurityUtil.hashPassword(password));
            user.setRole(UserRole.CUSTOMER);
            user.setActive(true);
            user.setCustomer(savedCustomer);

            userService.createUser(user);

            LOGGER.info("New customer registered: " + username);

            request.setAttribute("successMessage", "Registration successful! Please login.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);

        } catch (Exception e) {
            LOGGER.severe("Registration failed: " + e.getMessage());
            request.setAttribute("errorMessage", "Registration failed: " + e.getMessage());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
}