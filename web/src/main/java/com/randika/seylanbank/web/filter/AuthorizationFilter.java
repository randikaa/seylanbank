package com.randika.seylanbank.web.filter;

import com.randika.seylanbank.core.constants.SecurityConstants;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

@WebFilter(urlPatterns = {"/admin/*", "/customer/*"})
public class AuthorizationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthorizationFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("AuthorizationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
            return;
        }

        String userRole = (String) session.getAttribute("userRole");
        String path = httpRequest.getRequestURI();

        LOGGER.info("Checking authorization for user role: " + userRole + " accessing path: " + path);

        boolean authorized = false;

        if (path.contains("/admin/") &&
                (SecurityConstants.ADMIN_ROLE.equals(userRole) || SecurityConstants.SUPER_ADMIN_ROLE.equals(userRole))) {
            authorized = true;
        } else if (path.contains("/customer/") && SecurityConstants.CUSTOMER_ROLE.equals(userRole)) {
            authorized = true;
        }

        if (authorized) {
            chain.doFilter(request, response);
        } else {
            LOGGER.warning("Unauthorized access attempt by role " + userRole + " to " + path);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
        }
    }

    @Override
    public void destroy() {
        LOGGER.info("AuthorizationFilter destroyed");
    }
}