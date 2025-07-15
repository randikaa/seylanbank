package com.randika.seylanbank.banking.timer;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import com.randika.seylanbank.core.service.InterestCalculationService;

import java.util.logging.Logger;

@Singleton
@Startup
public class InterestCalculationTimer {

    private static final Logger LOGGER = Logger.getLogger(InterestCalculationTimer.class.getName());

    @EJB
    private InterestCalculationService interestCalculationService;

    @Schedule(dayOfMonth = "1", hour = "0", minute = "0", second = "0", persistent = false, info = "MonthlyInterest")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void performMonthlyInterestCalculation() {
        LOGGER.info("Starting monthly interest calculation");

        try {
            interestCalculationService.calculateInterestForAllAccounts();
            LOGGER.info("Monthly interest calculation completed successfully");

        } catch (Exception e) {
            LOGGER.severe("Error during monthly interest calculation: " + e.getMessage());
        }
    }

    @Schedule(dayOfWeek = "1", hour = "1", minute = "0", second = "0", persistent = false, info = "WeeklyInterest")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void performWeeklyInterestUpdate() {
        LOGGER.info("Starting weekly interest rate update check");

        try {
            // Check for interest rate updates or policy changes
            checkInterestRateUpdates();
            LOGGER.info("Weekly interest rate update check completed");

        } catch (Exception e) {
            LOGGER.severe("Error during weekly interest rate update: " + e.getMessage());
        }
    }

    private void checkInterestRateUpdates() {
        // Implementation for checking and applying interest rate updates
        LOGGER.info("Checking for interest rate updates");
    }
}