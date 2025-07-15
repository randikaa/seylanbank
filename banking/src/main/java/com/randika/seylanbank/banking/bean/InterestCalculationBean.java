package com.randika.seylanbank.banking.bean;

import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.annotation.security.RolesAllowed;

import com.randika.seylanbank.core.service.InterestCalculationService;
import com.randika.seylanbank.core.service.AccountService;
import com.randika.seylanbank.core.service.TransactionService;
import com.randika.seylanbank.core.model.*;
import com.randika.seylanbank.core.enums.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class InterestCalculationBean implements InterestCalculationService {

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @EJB
    private AccountService accountService;

    @EJB
    private TransactionService transactionService;

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void calculateInterestForAccount(Long accountId) {
        try {
            Account account = accountService.findAccountById(accountId);

            if (account.getAccountType() == AccountType.CURRENT) {
                return; // Current accounts don't earn interest
            }

            if (account.getStatus() != AccountStatus.ACTIVE) {
                return; // Only calculate interest for active accounts
            }

            BigDecimal interestAmount = calculateInterestAmount(account);

            if (interestAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Create interest credit transaction
                transactionService.processTransaction(
                        accountId,
                        TransactionType.INTEREST_CREDIT,
                        interestAmount
                );

                System.out.println("Interest calculated for account " + account.getAccountNumber() +
                        ": " + interestAmount);
            }
        } catch (Exception e) {
            System.err.println("Error calculating interest for account " + accountId +
                    ": " + e.getMessage());
        }
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void calculateInterestForAllAccounts() {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.status = :status " +
                        "AND a.accountType != :currentType", Account.class);
        query.setParameter("status", AccountStatus.ACTIVE);
        query.setParameter("currentType", AccountType.CURRENT);

        List<Account> accounts = query.getResultList();

        for (Account account : accounts) {
            calculateInterestForAccount(account.getId());
        }

        System.out.println("Interest calculation completed for " + accounts.size() + " accounts");
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "CUSTOMER"})
    public BigDecimal getInterestAmount(Long accountId, Date fromDate, Date toDate) {
        try {
            Account account = accountService.findAccountById(accountId);

            if (account.getAccountType() == AccountType.CURRENT) {
                return BigDecimal.ZERO;
            }

            // Calculate interest for the given period
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                    fromDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    toDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            );

            BigDecimal dailyRate = account.getInterestRate()
                    .divide(BigDecimal.valueOf(365), 6, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

            return account.getBalance()
                    .multiply(dailyRate)
                    .multiply(BigDecimal.valueOf(daysBetween))
                    .setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            System.err.println("Error calculating interest amount: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Override
    @RolesAllowed({"CUSTOMER"})
    public BigDecimal getMyInterestAmount(Long accountId, Date fromDate, Date toDate, Long customerId)
            throws Exception {
        Account account = accountService.findAccountById(accountId);
        if (!account.getCustomer().getId().equals(customerId)) {
            throw new Exception("Customer can only view interest for their own accounts");
        }

        return getInterestAmount(accountId, fromDate, toDate);
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void updateInterestRates(AccountType accountType, BigDecimal newRate) {
        TypedQuery<Account> query = em.createQuery(
                "UPDATE Account a SET a.interestRate = :newRate WHERE a.accountType = :accountType",
                Account.class);
        query.setParameter("newRate", newRate);
        query.setParameter("accountType", accountType);

        int updatedCount = query.executeUpdate();
        System.out.println("Updated interest rates for " + updatedCount + " " +
                accountType.getDisplayName() + " accounts");
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Account> getHighInterestAccounts(BigDecimal minimumBalance) {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.balance >= :minimumBalance " +
                        "AND a.accountType != :currentType ORDER BY a.balance DESC", Account.class);
        query.setParameter("minimumBalance", minimumBalance);
        query.setParameter("currentType", AccountType.CURRENT);

        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public BigDecimal getTotalInterestPaid(Date fromDate, Date toDate) {
        TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :interestType " +
                        "AND t.transactionDate BETWEEN :fromDate AND :toDate", BigDecimal.class);
        query.setParameter("interestType", TransactionType.INTEREST_CREDIT);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        BigDecimal result = query.getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Account> getAccountsEligibleForInterest() {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.status = :status " +
                        "AND a.accountType != :currentType " +
                        "AND a.balance > a.minimumBalance", Account.class);
        query.setParameter("status", AccountStatus.ACTIVE);
        query.setParameter("currentType", AccountType.CURRENT);

        return query.getResultList();
    }

    // Private helper methods
    private BigDecimal calculateInterestAmount(Account account) {
        BigDecimal balance = account.getBalance();
        BigDecimal interestRate = account.getInterestRate();

        // Different calculation methods based on account type
        switch (account.getAccountType()) {
            case SAVINGS:
                return calculateSavingsInterest(balance, interestRate);
            case FIXED_DEPOSIT:
                return calculateFixedDepositInterest(balance, interestRate);
            case CHECKING:
                return calculateCheckingInterest(balance, interestRate);
            default:
                return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateSavingsInterest(BigDecimal balance, BigDecimal interestRate) {
        // Monthly interest calculation for savings accounts
        BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        return balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFixedDepositInterest(BigDecimal balance, BigDecimal interestRate) {
        // Monthly compounding for fixed deposits
        BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        return balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCheckingInterest(BigDecimal balance, BigDecimal interestRate) {
        // Lower interest rate for checking accounts, calculated monthly
        BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        // Only calculate interest if balance is above minimum threshold
        if (balance.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }
}