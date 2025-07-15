package com.randika.seylanbank.core.service;

import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;

public interface NotificationService {
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    void sendTransactionNotification(Long customerId, String transactionDetails);

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    void sendLowBalanceAlert(Long accountId, BigDecimal currentBalance);

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    void sendScheduledTransferNotification(Long customerId, String transferDetails);

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    void sendAccountStatusChangeNotification(Long customerId, String statusChange);

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    void sendPasswordChangeNotification(Long userId);

    @RolesAllowed({"SUPER_ADMIN"})
    void sendSystemMaintenanceNotification(String maintenanceDetails);

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    void sendEmailNotification(String email, String subject, String content);

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    void sendSMSNotification(String phoneNumber, String message);
}
