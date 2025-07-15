package com.randika.seylanbank.banking.timer;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import com.randika.seylanbank.core.service.ScheduledOperationService;

import java.util.logging.Logger;

@Singleton
@Startup
public class ScheduledTransferTimer {

    private static final Logger LOGGER = Logger.getLogger(ScheduledTransferTimer.class.getName());

    @EJB
    private ScheduledOperationService scheduledOperationService;

    @Schedule(minute = "*/15", hour = "*", persistent = false, info = "ScheduledTransfers")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processScheduledTransfers() {
        LOGGER.info("Processing scheduled transfers");

        try {
            scheduledOperationService.processPendingTransfers();
            LOGGER.info("Scheduled transfers processing completed");

        } catch (Exception e) {
            LOGGER.severe("Error processing scheduled transfers: " + e.getMessage());
        }
    }

    @Schedule(hour = "*/6", minute = "0", second = "0", persistent = false, info = "TransferCleanup")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cleanupFailedTransfers() {
        LOGGER.info("Cleaning up failed transfers");

        try {
            // Clean up failed or expired transfer requests
            performTransferCleanup();
            LOGGER.info("Transfer cleanup completed");

        } catch (Exception e) {
            LOGGER.severe("Error during transfer cleanup: " + e.getMessage());
        }
    }

    private void performTransferCleanup() {
        // Implementation for cleaning up failed transfers
        LOGGER.info("Performing transfer cleanup operations");
    }
}