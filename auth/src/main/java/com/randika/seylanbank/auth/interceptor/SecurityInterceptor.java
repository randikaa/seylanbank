package com.randika.seylanbank.auth.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJBAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import com.randika.seylanbank.core.exception.UnauthorizedAccessException;

import java.util.logging.Logger;
import java.util.Arrays;

@Interceptor
@SecurityBinding
public class SecurityInterceptor {

    private static final Logger LOGGER = Logger.getLogger(SecurityInterceptor.class.getName());

    @Context
    private HttpServletRequest httpRequest;

    @AroundInvoke
    public Object secureMethod(InvocationContext context) throws Exception {
        String methodName = context.getMethod().getName();
        String className = context.getTarget().getClass().getSimpleName();

        LOGGER.info("Security check for method: " + className + "." + methodName);

        RolesAllowed rolesAllowed = context.getMethod().getAnnotation(RolesAllowed.class);

        if (rolesAllowed != null) {
            String[] allowedRoles = rolesAllowed.value();
            LOGGER.info("Required roles: " + Arrays.toString(allowedRoles));

            boolean hasRole = false;
            if (httpRequest != null) {
                for (String role : allowedRoles) {
                    if (httpRequest.isUserInRole(role)) {
                        hasRole = true;
                        LOGGER.info("User has required role: " + role);
                        break;
                    }
                }
            }

            if (!hasRole) {
                String userName = (httpRequest != null && httpRequest.getUserPrincipal() != null)
                        ? httpRequest.getUserPrincipal().getName() : "unknown";
                LOGGER.warning("Access denied for user: " + userName +
                        " to method: " + className + "." + methodName);
                throw new UnauthorizedAccessException("Access denied to method: " + methodName);
            }
        }

        return context.proceed();
    }
}