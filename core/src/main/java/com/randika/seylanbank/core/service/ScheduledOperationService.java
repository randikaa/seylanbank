package com.randika.seylanbank.core.service;

import com.randika.seylanbank.core.model.ScheduledTask;

import jakarta.ejb.Remote;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Remote
public interface ScheduledOperationService {
    void scheduleTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount, Date scheduledDate);
    void processPendingTransfers();
    void processScheduledTasks();

    // Customer methods
    void customerScheduleTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount,
                                  Date scheduledDate, Long customerId) throws Exception;
    List<ScheduledTask> getMyScheduledTasks(Long customerId);
    void cancelMyScheduledTask(Long taskId, Long customerId) throws Exception;

    // Admin methods
    List<ScheduledTask> getPendingTasks();
    List<ScheduledTask> getTasksByUser(String username);
    void cancelScheduledTask(Long taskId);
}