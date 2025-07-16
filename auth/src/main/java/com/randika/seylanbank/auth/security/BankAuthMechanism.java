package com.randika.seylanbank.auth.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@AutoApplySession
@ApplicationScoped
public class BankAuthMechanism implements HttpAuthenticationMechanism {

    private static final Logger LOGGER = Logger.getLogger(BankAuthMechanism.class.getName());

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request,
                                                HttpServletResponse response, HttpMessageContext httpMessageContext) {

        String requestURI = request.getRequestURI();
        LOGGER.info("Validating request for URI: " + requestURI);

        // Check if resource is protected
        if (httpMessageContext.isProtected()) {
            // Check for existing authentication
            if (httpMessageContext.getCallerPrincipal() != null) {
                LOGGER.info("User already authenticated: " + httpMessageContext.getCallerPrincipal().getName());
                return AuthenticationStatus.SUCCESS;
            }

            // Check for form-based authentication
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (username != null && password != null) {
                LOGGER.info("Attempting form-based authentication for user: " + username);

                // ===== Manual authentication logic =====
                // Replace this with your actual authentication logic (e.g., DB check)
                boolean validUser = "admin".equals(username) && "adminpass".equals(password);
                if (validUser) {
                    Set<String> roles = new HashSet<>();
                    roles.add("USER");  // Assign roles accordingly

                    return httpMessageContext.notifyContainerAboutLogin(username, roles);
                } else {
                    try {
                        String loginURL = request.getContextPath() + "/login.jsp?error=1";
                        LOGGER.info("Authentication failed, redirecting to: " + loginURL);
                        response.sendRedirect(loginURL);
                        return AuthenticationStatus.SEND_FAILURE;
                    } catch (Exception e) {
                        LOGGER.severe("Error redirecting to login page after failed login: " + e.getMessage());
                        return AuthenticationStatus.SEND_FAILURE;
                    }
                }
            }

            // Redirect to login page if not authenticated
            try {
                String loginURL = request.getContextPath() + "/login.jsp";
                LOGGER.info("Redirecting to login page: " + loginURL);
                response.sendRedirect(loginURL);
                return AuthenticationStatus.SEND_CONTINUE;
            } catch (Exception e) {
                LOGGER.severe("Error redirecting to login page: " + e.getMessage());
                return AuthenticationStatus.SEND_FAILURE;
            }
        }

        return AuthenticationStatus.NOT_DONE;
    }

    @Override
    public AuthenticationStatus secureResponse(HttpServletRequest request,
                                               HttpServletResponse response, HttpMessageContext httpMessageContext) {
        return AuthenticationStatus.SUCCESS;
    }

    @Override
    public void cleanSubject(HttpServletRequest request, HttpServletResponse response,
                             HttpMessageContext httpMessageContext) {
        LOGGER.info("Cleaning subject for logout");
        request.getSession().invalidate();
    }
}
