package com.randika.seylanbank.web.servlet;

import com.randika.seylanbank.core.service.UserService;
import com.randika.seylanbank.core.model.User;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @EJB
    private UserService userService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            User user = userService.authenticateUser(username, password);

            if (user != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("userRole", user.getRole().name());

                LOGGER.info("User logged in successfully: " + username);

                // Redirect based on role
                switch (user.getRole()) {
                    case SUPER_ADMIN:
                    case ADMIN:
                        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                        break;
                    case CUSTOMER:
                        response.sendRedirect(request.getContextPath() + "/customer/dashboard");
                        break;
                    default:
                        response.sendRedirect(request.getContextPath() + "/index.jsp");
                }
            } else {
                LOGGER.warning("Failed login attempt for username: " + username);
                request.setAttribute("errorMessage", "Invalid username or password");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            LOGGER.severe("Error during login: " + e.getMessage());
            request.setAttribute("errorMessage", "System error during login");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}