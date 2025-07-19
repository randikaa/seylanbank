package com.randika.seylanbank.banking.timer;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import com.randika.seylanbank.core.service.InterestCalculationService;
import com.randika.seylanbank.core.service.ScheduledOperationService;

import java.util.logging.Logger;

@Singleton
@Startup
public class DailyBalanceUpdateTimer {
    private static final Logger LOGGER = Logger.getLogger(DailyBalanceUpdateTimer.class.getName());
    @EJB
    private InterestCalculationService interestCalculationService;
    @EJB
    private ScheduledOperationService scheduledOperationService;

    @Schedule(hour = "23", minute = "59", second = "0", persistent = false, info = "DailyBalanceUpdate")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void performDailyBalanceUpdate() {
        LOGGER.info("Starting daily balance update timer");

        try {
            scheduledOperationService.processPendingTransfers();
            interestCalculationService.calculateInterestForAllAccounts();
            LOGGER.info("Daily balance update completed successfully");
        } catch (Exception e) {
            LOGGER.severe("Error during daily balance update: " + e.getMessage());
        }
    }

    @Schedule(hour = "2", minute = "0", second = "0", persistent = false, info = "SystemMaintenance")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void performSystemMaintenance() {
        LOGGER.info("Starting system maintenance");

        try {
            // Cleanup expired sessions, archive old data, etc.
            performDataCleanup();

            LOGGER.info("System maintenance completed successfully");

        } catch (Exception e) {
            LOGGER.severe("Error during system maintenance: " + e.getMessage());
        }
    }

    private void performDataCleanup() {
        LOGGER.info("Performing data cleanup tasks");
    }
}

