package com.randika.seylanbank.auth.security;

import com.randika.seylanbank.core.model.User;
import com.randika.seylanbank.core.util.SecurityUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static com.randika.seylanbank.core.enums.UserRole.*;

@AutoApplySession
@ApplicationScoped
public class BankAuthMechanism implements HttpAuthenticationMechanism {

    private static final Logger LOGGER = Logger.getLogger(BankAuthMechanism.class.getName());

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request,
                                                HttpServletResponse response,
                                                HttpMessageContext httpMessageContext) {
        String requestURI = request.getRequestURI();
        LOGGER.info("Validating request for URI: " + requestURI);

        if (httpMessageContext.isProtected()) {
            if (httpMessageContext.getCallerPrincipal() != null) {
                LOGGER.info("User already authenticated: " + httpMessageContext.getCallerPrincipal().getName());
                return AuthenticationStatus.SUCCESS;
            }

            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (username != null && password != null) {
                LOGGER.info("Attempting form-based authentication for user: " + username);

                try {
                    // Lookup user from DB
                    TypedQuery<User> query = em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username", User.class);
                    query.setParameter("username", username);
                    User user = query.getSingleResult();

                    if (SecurityUtil.verifyPassword(password, user.getPassword(), user.getSalt())) {
                        Set<String> roles = new HashSet<>();
                        roles.add(user.getRole().name());
                        httpMessageContext.notifyContainerAboutLogin(username, roles);

                        String redirectURL;
                        switch (user.getRole()) {
                            case ADMIN:
                                redirectURL = "/admin/dashboard.jsp";
                                break;
                            case SUPER_ADMIN:
                                redirectURL = "/admin/system-settings.jsp";
                                break;
                            case CUSTOMER:
                                redirectURL = "/customer/dashboard.jsp";
                                break;
                            default:
                                redirectURL = "/unauthorized.jsp";
                                break;
                        }

                        response.sendRedirect(request.getContextPath() + redirectURL);
                        return AuthenticationStatus.SEND_FAILURE;
                    }
                } catch (NoResultException e) {
                    LOGGER.warning("Invalid username: " + username);
                } catch (Exception e) {
                    LOGGER.severe("Authentication error: " + e.getMessage());
                }

                try {
                    response.sendRedirect(request.getContextPath() + "/login.jsp?error=1");
                } catch (IOException e) {
                    LOGGER.severe("Failed to redirect after failed login: " + e.getMessage());
                }
                return AuthenticationStatus.SEND_FAILURE;
            }

            try {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            } catch (IOException e) {
                LOGGER.severe("Error redirecting to login page: " + e.getMessage());
            }
            return AuthenticationStatus.SEND_CONTINUE;
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
