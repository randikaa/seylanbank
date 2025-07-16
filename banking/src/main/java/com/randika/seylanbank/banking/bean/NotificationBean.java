package com.randika.seylanbank.banking.bean;

import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.Resource;

import com.randika.seylanbank.core.service.NotificationService;
import com.randika.seylanbank.core.model.Customer;
import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.model.User;

import java.math.BigDecimal;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class NotificationBean implements NotificationService {

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @Resource(mappedName = "mail/Default")
    private jakarta.mail.Session mailSession;

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @Override
    public void sendTransactionNotification(Long customerId, String transactionDetails) {
        try {
            Customer customer = em.find(Customer.class, customerId);
            if (customer != null && customer.getUser() != null) {
                String email = customer.getUser().getEmail();
                String subject = "Transaction Notification - SeylanBank";
                String content = "Dear " + customer.getFullName() + ",\n\n" +
                        "Transaction Details: " + transactionDetails + "\n\n" +
                        "Thank you for banking with SeylanBank.";

                sendEmailNotification(email, subject, content);
            }
        } catch (Exception e) {
            System.err.println("Error sending transaction notification: " + e.getMessage());
        }
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @Override
    public void sendLowBalanceAlert(Long accountId, BigDecimal currentBalance) {
        try {
            Account account = em.find(Account.class, accountId);
            if (account != null) {
                Customer customer = account.getCustomer();
                String email = customer.getUser().getEmail();
                String subject = "Low Balance Alert - SeylanBank";
                String content = "Dear " + customer.getFullName() + ",\n\n" +
                        "Your account " + account.getAccountNumber() +
                        " has a low balance of " + currentBalance + ".\n\n" +
                        "Please add funds to avoid any service interruptions.";

                sendEmailNotification(email, subject, content);
            }
        } catch (Exception e) {
            System.err.println("Error sending low balance alert: " + e.getMessage());
        }
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @Override
    public void sendScheduledTransferNotification(Long customerId, String transferDetails) {
        try {
            Customer customer = em.find(Customer.class, customerId);
            if (customer != null && customer.getUser() != null) {
                String email = customer.getUser().getEmail();
                String subject = "Scheduled Transfer Notification - SeylanBank";
                String content = "Dear " + customer.getFullName() + ",\n\n" +
                        "Your scheduled transfer has been processed: " + transferDetails + "\n\n" +
                        "Thank you for banking with SeylanBank.";

                sendEmailNotification(email, subject, content);
            }
        } catch (Exception e) {
            System.err.println("Error sending scheduled transfer notification: " + e.getMessage());
        }
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @Override
    public void sendAccountStatusChangeNotification(Long customerId, String statusChange) {
        try {
            Customer customer = em.find(Customer.class, customerId);
            if (customer != null && customer.getUser() != null) {
                String email = customer.getUser().getEmail();
                String subject = "Account Status Change - SeylanBank";
                String content = "Dear " + customer.getFullName() + ",\n\n" +
                        "Your account status has been changed: " + statusChange + "\n\n" +
                        "If you have any questions, please contact customer service.";

                sendEmailNotification(email, subject, content);
            }
        } catch (Exception e) {
            System.err.println("Error sending account status change notification: " + e.getMessage());
        }
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @Override
    public void sendPasswordChangeNotification(Long userId) {
        try {
            User user = em.find(User.class, userId);
            if (user != null) {
                String subject = "Password Changed - SeylanBank";
                String content = "Dear User,\n\n" +
                        "Your password has been successfully changed.\n\n" +
                        "If you did not make this change, please contact customer service immediately.";

                sendEmailNotification(user.getEmail(), subject, content);
            }
        } catch (Exception e) {
            System.err.println("Error sending password change notification: " + e.getMessage());
        }
    }

    @RolesAllowed({"SUPER_ADMIN"})
    @Override
    public void sendSystemMaintenanceNotification(String maintenanceDetails) {
        try {
            // Send to all active users (implementation would be more sophisticated)
            String subject = "System Maintenance Notification - SeylanBank";
            String content = "Dear Valued Customer,\n\n" +
                    "System Maintenance: " + maintenanceDetails + "\n\n" +
                    "We apologize for any inconvenience.";

            // This would typically send to a mailing list or all users
            System.out.println("System maintenance notification: " + content);
        } catch (Exception e) {
            System.err.println("Error sending system maintenance notification: " + e.getMessage());
        }
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @Override
    public void sendEmailNotification(String email, String subject, String content) {
        try {
            // Implementation would use JavaMail API
            jakarta.mail.Message message = new jakarta.mail.internet.MimeMessage(mailSession);
            message.setFrom(new jakarta.mail.internet.InternetAddress("noreply@seylanbank.com"));
            message.setRecipients(jakarta.mail.Message.RecipientType.TO,
                    jakarta.mail.internet.InternetAddress.parse(email));
            message.setSubject(subject);
            message.setText(content);

            // Jakarta.mail.Transport.send(message);

            System.out.println("Email sent to: " + email + " - Subject: " + subject);
        } catch (Exception e) {
            System.err.println("Error sending email notification: " + e.getMessage());
        }
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @Override
    public void sendSMSNotification(String phoneNumber, String message) {
        try {
            // Implementation would integrate with SMS gateway
            System.out.println("SMS sent to: " + phoneNumber + " - Message: " + message);
        } catch (Exception e) {
            System.err.println("Error sending SMS notification: " + e.getMessage());
        }
    }
}