package com.randika.seylanbank.banking.bean;

import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.Resource;

import com.randika.seylanbank.core.service.ScheduledOperationService;
import com.randika.seylanbank.core.service.TransactionService;
import com.randika.seylanbank.core.service.InterestCalculationService;
import com.randika.seylanbank.core.model.*;
import com.randika.seylanbank.core.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ScheduledOperationsBean implements ScheduledOperationService {

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @EJB
    private TransactionService transactionService;

    @EJB
    private InterestCalculationService interestService;

    @Resource
    private TimerService timerService;

    // Automatic scheduled methods using @Schedule

    // Daily balance update at 11:59 PM
    @Schedule(hour = "23", minute = "59", second = "0", persistent = false)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void dailyBalanceUpdate() {
        try {
            performDailyMaintenance();
            System.out.println("Daily balance update completed at: " + LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Error in daily balance update: " + e.getMessage());
        }
    }

    // Monthly interest calculation on 1st of each month
    @Schedule(dayOfMonth = "1", hour = "0", minute = "0", second = "0", persistent = false)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void monthlyInterestCalculation() {
        try {
            interestService.calculateInterestForAllAccounts();
            System.out.println("Monthly interest calculation completed at: " + LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Error in monthly interest calculation: " + e.getMessage());
        }
    }

    // Process scheduled transfers every 15 minutes
    @Schedule(minute = "*/15", hour = "*", persistent = false)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processScheduledTransfers() {
        try {
            processPendingTransfers();
            System.out.println("Scheduled transfers processed at: " + LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Error processing scheduled transfers: " + e.getMessage());
        }
    }

    // System maintenance at 2 AM daily
    @Schedule(hour = "2", minute = "0", second = "0", persistent = false)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void systemMaintenance() {
        try {
            cleanupExpiredSessions();
            archiveOldTransactions();
            performSystemHealthCheck();
            System.out.println("System maintenance completed at: " + LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Error in system maintenance: " + e.getMessage());
        }
    }

    // Programmatic timer creation for user-specific scheduled transfers
    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "CUSTOMER"})
    public void scheduleTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount, Date scheduledDate) {
        // Create scheduled task record
        ScheduledTask task = new ScheduledTask(
                "FUND_TRANSFER",
                fromAccountId,
                toAccountId,
                amount,
                scheduledDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                "Scheduled fund transfer"
        );
        task.setCreatedDate(LocalDateTime.now());
        em.persist(task);

        // Create programmatic timer
        TimerConfig config = new TimerConfig();
        config.setInfo("SCHEDULED_TRANSFER_" + task.getId());
        config.setPersistent(false);

        Timer timer = timerService.createSingleActionTimer(scheduledDate, config);

        System.out.println("Scheduled transfer created for: " + scheduledDate +
                " from account " + fromAccountId + " to account " + toAccountId);
    }

    // Timer callback method for programmatic timers
    @Timeout
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void handleTimeout(Timer timer) {
        try {
            String info = (String) timer.getInfo();

            if (info.startsWith("SCHEDULED_TRANSFER_")) {
                Long taskId = Long.parseLong(info.substring("SCHEDULED_TRANSFER_".length()));
                executeScheduledTransfer(taskId);
            }
        } catch (Exception e) {
            System.err.println("Error handling timer timeout: " + e.getMessage());
        }
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void processPendingTransfers() {
        TypedQuery<ScheduledTask> query = em.createQuery(
                "SELECT st FROM ScheduledTask st WHERE st.executed = false " +
                        "AND st.scheduledDate <= :now AND st.taskType = 'FUND_TRANSFER'",
                ScheduledTask.class);
        query.setParameter("now", LocalDateTime.now());

        List<ScheduledTask> pendingTasks = query.getResultList();

        for (ScheduledTask task : pendingTasks) {
            try {
                executeScheduledTransfer(task.getId());
            } catch (Exception e) {
                System.err.println("Failed to execute scheduled transfer: " + task.getId() +
                        " - " + e.getMessage());
            }
        }
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void processScheduledTasks() {
        processPendingTransfers();
    }

    @Override
    @RolesAllowed({"CUSTOMER"})
    public void customerScheduleTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount,
                                         Date scheduledDate, Long customerId) throws Exception {
        // Verify customer owns the from account
        Account fromAccount = em.find(Account.class, fromAccountId);
        if (fromAccount == null || !fromAccount.getCustomer().getId().equals(customerId)) {
            throw new Exception("Customer can only schedule transfers from their own accounts");
        }

        scheduleTransfer(fromAccountId, toAccountId, amount, scheduledDate);
    }

    @Override
    @RolesAllowed({"CUSTOMER"})
    public List<ScheduledTask> getMyScheduledTasks(Long customerId) {
        TypedQuery<ScheduledTask> query = em.createQuery(
                "SELECT st FROM ScheduledTask st JOIN Account a ON st.fromAccountId = a.id " +
                        "WHERE a.customer.id = :customerId ORDER BY st.scheduledDate DESC", ScheduledTask.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"CUSTOMER"})
    public void cancelMyScheduledTask(Long taskId, Long customerId) throws Exception {
        ScheduledTask task = em.find(ScheduledTask.class, taskId);
        if (task == null) {
            throw new Exception("Scheduled task not found");
        }

        // Verify customer owns the from account
        Account fromAccount = em.find(Account.class, task.getFromAccountId());
        if (fromAccount == null || !fromAccount.getCustomer().getId().equals(customerId)) {
            throw new Exception("Customer can only cancel their own scheduled tasks");
        }

        if (!task.isExecuted()) {
            em.remove(task);
            System.out.println("Customer cancelled scheduled task: " + taskId);
        } else {
            throw new Exception("Cannot cancel already executed task");
        }
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<ScheduledTask> getPendingTasks() {
        TypedQuery<ScheduledTask> query = em.createQuery(
                "SELECT st FROM ScheduledTask st WHERE st.executed = false " +
                        "ORDER BY st.scheduledDate ASC", ScheduledTask.class);
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "CUSTOMER"})
    public List<ScheduledTask> getTasksByUser(String username) {
        TypedQuery<ScheduledTask> query = em.createQuery(
                "SELECT st FROM ScheduledTask st WHERE st.createdBy = :username " +
                        "ORDER BY st.scheduledDate DESC", ScheduledTask.class);
        query.setParameter("username", username);
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void cancelScheduledTask(Long taskId) {
        ScheduledTask task = em.find(ScheduledTask.class, taskId);
        if (task != null && !task.isExecuted()) {
            em.remove(task);
            System.out.println("Cancelled scheduled task: " + taskId);
        }
    }

    // Private helper methods
    private void executeScheduledTransfer(Long taskId) {
        ScheduledTask task = em.find(ScheduledTask.class, taskId);
        if (task == null || task.isExecuted()) {
            return;
        }

        try {
            transactionService.transferFunds(
                    task.getFromAccountId(),
                    task.getToAccountId(),
                    task.getAmount()
            );

            task.setExecuted(true);
            task.setExecutedDate(LocalDateTime.now());
            em.merge(task);

            System.out.println("Executed scheduled transfer: " + taskId);
        } catch (Exception e) {
            System.err.println("Failed to execute scheduled transfer " + taskId + ": " + e.getMessage());
        }
    }

    private void performDailyMaintenance() {
        interestService.calculateInterestForAllAccounts();
        updateInactiveAccounts();
        processPendingTransfers();
    }

    private void updateInactiveAccounts() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);

        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.lastTransactionDate < :cutoffDate " +
                        "AND a.status = :activeStatus", Account.class);
        query.setParameter("cutoffDate", cutoffDate);
        query.setParameter("activeStatus", AccountStatus.ACTIVE);

        List<Account> inactiveAccounts = query.getResultList();

        for (Account account : inactiveAccounts) {
            account.setStatus(AccountStatus.INACTIVE);
            em.merge(account);
        }

        System.out.println("Updated " + inactiveAccounts.size() + " accounts to inactive status");
    }

    private void cleanupExpiredSessions() {
        System.out.println("Cleaning up expired sessions...");
    }

    private void archiveOldTransactions() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(12);

        TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.transactionDate < :cutoffDate",
                Transaction.class);
        query.setParameter("cutoffDate", cutoffDate);

        List<Transaction> oldTransactions = query.getResultList();
        System.out.println("Found " + oldTransactions.size() + " transactions to archive");
    }

    private void performSystemHealthCheck() {
        try {
            em.createQuery("SELECT COUNT(a) FROM Account a").getSingleResult();
            System.out.println("Database health check: OK");
        } catch (Exception e) {
            System.err.println("Database health check failed: " + e.getMessage());
        }
    }
}