package com.randika.seylanbank.auth.security;

import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.randika.seylanbank.core.constants.SecurityConstants;

import java.io.IOException;
import java.util.logging.Logger;

@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(SecurityFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Security Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        LOGGER.info("Processing request: " + requestURI);

        // Allow access to public resources
        if (isPublicResource(requestURI, contextPath)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session == null || httpRequest.getUserPrincipal() == null) {
            LOGGER.warning("Unauthenticated access attempt to: " + requestURI);
            httpResponse.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        if (!hasRequiredRole(httpRequest, requestURI)) {
            LOGGER.warning("Unauthorized access attempt by user: " +
                    httpRequest.getUserPrincipal().getName() + " to: " + requestURI);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        addSecurityHeaders(httpResponse);

        chain.doFilter(request, response);
    }

    private boolean isPublicResource(String requestURI, String contextPath) {
        String[] publicPaths = {
                "/login.jsp", "/register.jsp", "/error.jsp", "/assets/",
                "/css/", "/js/", "/images/", "/favicon.ico"
        };

        for (String path : publicPaths) {
            if (requestURI.startsWith(contextPath + path)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRequiredRole(HttpServletRequest request, String requestURI) {
        String contextPath = request.getContextPath();

        if (requestURI.startsWith(contextPath + "/admin/")) {
            return request.isUserInRole(SecurityConstants.ADMIN_ROLE) ||
                    request.isUserInRole(SecurityConstants.SUPER_ADMIN_ROLE);
        }

        if (requestURI.startsWith(contextPath + "/customer/")) {
            return request.isUserInRole(SecurityConstants.CUSTOMER_ROLE);
        }

        return true; // Allow access to other resources if authenticated
    }

    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
    }

    @Override
    public void destroy() {
        LOGGER.info("Security Filter destroyed");
    }
}