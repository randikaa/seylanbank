package com.randika.seylanbank.auth.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import com.randika.seylanbank.core.model.AuditLog;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Interceptor
@Auditable
public class AuditInterceptor {

    private static final Logger LOGGER = Logger.getLogger(AuditInterceptor.class.getName());

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @Context
    private HttpServletRequest httpRequest;

    @AroundInvoke
    public Object auditMethod(InvocationContext context) throws Exception {
        String methodName = context.getMethod().getName();
        String className = context.getTarget().getClass().getSimpleName();
        String userName = getUserName();

        LOGGER.info("Auditing method: " + className + "." + methodName + " by user: " + userName);

        LocalDateTime startTime = LocalDateTime.now();
        boolean success = false;
        String errorMessage = null;

        try {
            Object result = context.proceed();
            success = true;
            return result;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            LOGGER.warning("Method " + className + "." + methodName + " failed: " + errorMessage);
            throw e;
        } finally {
            // Log audit entry
            try {
                logAuditEntry(className, methodName, userName, startTime, success, errorMessage);
            } catch (Exception e) {
                LOGGER.severe("Failed to log audit entry: " + e.getMessage());
            }
        }
    }

    private String getUserName() {
        if (httpRequest != null && httpRequest.getUserPrincipal() != null) {
            return httpRequest.getUserPrincipal().getName();
        }
        return "system";
    }

    private void logAuditEntry(String className, String methodName, String userName,
                               LocalDateTime timestamp, boolean success, String errorMessage) {

        AuditLog auditLog = new AuditLog();
        auditLog.setAction(className + "." + methodName);
        auditLog.setUserName(userName);
        auditLog.setTimestamp(timestamp);
        auditLog.setSuccess(success);
        auditLog.setErrorMessage(errorMessage);
        auditLog.setIpAddress(getClientIP());

        em.persist(auditLog);
    }

    private String getClientIP() {
        if (httpRequest != null) {
            String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return httpRequest.getRemoteAddr();
        }
        return "unknown";
    }
}