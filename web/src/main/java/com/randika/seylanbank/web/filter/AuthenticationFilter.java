package com.randika.seylanbank.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

@WebFilter(urlPatterns = {"/admin/*", "/customer/*", "/account/*", "/transaction/*", "/report/*"})
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("AuthenticationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        HttpSession session = httpRequest.getSession(false);

        LOGGER.info("Checking authentication for path: " + path);

        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);
        boolean isLoginRequest = path.endsWith("/login") || path.endsWith("/login.jsp");
        boolean isPublicResource = isPublicResource(path);

        if (isLoggedIn || isLoginRequest || isPublicResource) {
            chain.doFilter(request, response);
        } else {
            LOGGER.warning("Unauthenticated access attempt to: " + path);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
        }
    }

    private boolean isPublicResource(String path) {
        return path.endsWith("/index.jsp") ||
                path.endsWith("/register") ||
                path.endsWith("/register.jsp") ||
                path.contains("/assets/") ||
                path.contains("/css/") ||
                path.contains("/js/") ||
                path.contains("/images/");
    }

    @Override
    public void destroy() {
        LOGGER.info("AuthenticationFilter destroyed");
    }
}