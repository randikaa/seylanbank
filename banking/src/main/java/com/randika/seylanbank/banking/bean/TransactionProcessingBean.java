package com.randika.seylanbank.banking.bean;

import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.annotation.security.RolesAllowed;

import com.randika.seylanbank.core.service.TransactionService;
import com.randika.seylanbank.core.service.AccountService;
import com.randika.seylanbank.core.model.*;
import com.randika.seylanbank.core.enums.*;
import com.randika.seylanbank.core.exception.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TransactionProcessingBean implements TransactionService {

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @EJB
    private AccountService accountService;

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public Transaction processTransaction(Long accountId, TransactionType type, BigDecimal amount)
            throws AccountNotFoundException, InsufficientFundsException {

        Account account = accountService.findAccountById(accountId);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }

        Transaction transaction = new Transaction(account, type, amount, type.getDisplayName());

        // Validate and process based on transaction type
        switch (type) {
            case WITHDRAWAL:
                processWithdrawal(account, transaction);
                break;
            case DEPOSIT:
                processDeposit(account, transaction);
                break;
            case INTEREST_CREDIT:
                processInterestCredit(account, transaction);
                break;
            case FEE_DEBIT:
                processFeeDebit(account, transaction);
                break;
        }

        // Update account balance and last transaction date
        account.setLastTransactionDate(LocalDateTime.now());
        transaction.setBalanceAfter(account.getBalance());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setProcessedDate(LocalDateTime.now());

        em.persist(transaction);
        em.merge(account);

        return transaction;
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "CUSTOMER"})
    public void transferFunds(Long fromAccountId, Long toAccountId, BigDecimal amount)
            throws AccountNotFoundException, InsufficientFundsException {

        Account fromAccount = accountService.findAccountById(fromAccountId);
        Account toAccount = accountService.findAccountById(toAccountId);

        if (fromAccount.getStatus() != AccountStatus.ACTIVE ||
                toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Both accounts must be active for transfer");
        }

        if (!fromAccount.canWithdraw(amount)) {
            throw new InsufficientFundsException(
                    "Insufficient funds for transfer"
            );
        }

        // Create transfer transactions
        Transaction debitTransaction = new Transaction(fromAccount, TransactionType.TRANSFER,
                amount, "Transfer to " + toAccount.getAccountNumber());
        debitTransaction.setToAccount(toAccount);

        Transaction creditTransaction = new Transaction(toAccount, TransactionType.TRANSFER,
                amount, "Transfer from " + fromAccount.getAccountNumber());
        creditTransaction.setToAccount(fromAccount);

        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        // Set transaction details
        LocalDateTime now = LocalDateTime.now();
        debitTransaction.setBalanceAfter(fromAccount.getBalance());
        creditTransaction.setBalanceAfter(toAccount.getBalance());

        debitTransaction.setStatus(TransactionStatus.COMPLETED);
        creditTransaction.setStatus(TransactionStatus.COMPLETED);

        debitTransaction.setProcessedDate(now);
        creditTransaction.setProcessedDate(now);

        fromAccount.setLastTransactionDate(now);
        toAccount.setLastTransactionDate(now);

        em.persist(debitTransaction);
        em.persist(creditTransaction);
        em.merge(fromAccount);
        em.merge(toAccount);
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "CUSTOMER"})
    public List<Transaction> getTransactionHistory(Long accountId, Date fromDate, Date toDate)
            throws AccountNotFoundException {

        accountService.findAccountById(accountId); // Validate account exists

        TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.account.id = :accountId " +
                        "AND t.transactionDate BETWEEN :fromDate AND :toDate " +
                        "ORDER BY t.transactionDate DESC", Transaction.class);

        query.setParameter("accountId", accountId);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void reverseTransaction(Long transactionId) throws Exception {
        Transaction transaction = em.find(Transaction.class, transactionId);
        if (transaction == null) {
            throw new Exception("Transaction not found");
        }

        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new Exception("Only completed transactions can be reversed");
        }

        // Create reverse transaction
        TransactionType reverseType = getReverseTransactionType(transaction.getType());
        BigDecimal reverseAmount = transaction.getAmount();

        Transaction reverseTransaction = new Transaction(
                transaction.getAccount(),
                reverseType,
                reverseAmount,
                "Reversal of transaction: " + transaction.getTransactionId()
        );

        reverseTransaction.setReferenceNumber(transaction.getTransactionId());
        reverseTransaction.setStatus(TransactionStatus.COMPLETED);
        reverseTransaction.setProcessedDate(LocalDateTime.now());

        // Update account balance
        Account account = transaction.getAccount();
        if (reverseType == TransactionType.DEPOSIT) {
            account.setBalance(account.getBalance().add(reverseAmount));
        } else {
            account.setBalance(account.getBalance().subtract(reverseAmount));
        }

        reverseTransaction.setBalanceAfter(account.getBalance());

        em.persist(reverseTransaction);
        em.merge(account);
    }

    @Override
    @RolesAllowed({"CUSTOMER"})
    public void customerTransferFunds(Long fromAccountId, Long toAccountId, BigDecimal amount, Long customerId)
            throws AccountNotFoundException, InsufficientFundsException, UnauthorizedAccessException {

        // Verify customer owns the from account
        Account fromAccount = accountService.findAccountById(fromAccountId);
        if (!fromAccount.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedAccessException("Customer can only transfer from their own accounts");
        }

        transferFunds(fromAccountId, toAccountId, amount);
    }

    @Override
    @RolesAllowed({"CUSTOMER"})
    public List<Transaction> getMyTransactionHistory(Long accountId, Date fromDate, Date toDate, Long customerId)
            throws AccountNotFoundException, UnauthorizedAccessException {

        Account account = accountService.findAccountById(accountId);
        if (!account.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedAccessException("Customer can only view their own transaction history");
        }

        return getTransactionHistory(accountId, fromDate, toDate);
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Transaction> getAllTransactions(Date fromDate, Date toDate) {
        TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :fromDate AND :toDate " +
                        "ORDER BY t.transactionDate DESC", Transaction.class);

        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Transaction> getTransactionsByType(TransactionType type, Date fromDate, Date toDate) {
        TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.type = :type " +
                        "AND t.transactionDate BETWEEN :fromDate AND :toDate " +
                        "ORDER BY t.transactionDate DESC", Transaction.class);

        query.setParameter("type", type);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Transaction> getLargeTransactions(BigDecimal minimumAmount, Date fromDate, Date toDate) {
        TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.amount >= :minimumAmount " +
                        "AND t.transactionDate BETWEEN :fromDate AND :toDate " +
                        "ORDER BY t.amount DESC", Transaction.class);

        query.setParameter("minimumAmount", minimumAmount);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        return query.getResultList();
    }

    // Private helper methods
    private void processWithdrawal(Account account, Transaction transaction)
            throws InsufficientFundsException {
        if (!account.canWithdraw(transaction.getAmount())) {
            throw new InsufficientFundsException(
                    "Insufficient funds for withdrawal"
            );
        }
        account.setBalance(account.getBalance().subtract(transaction.getAmount()));
    }

    private void processDeposit(Account account, Transaction transaction) {
        account.setBalance(account.getBalance().add(transaction.getAmount()));
    }

    private void processInterestCredit(Account account, Transaction transaction) {
        account.setBalance(account.getBalance().add(transaction.getAmount()));
    }

    private void processFeeDebit(Account account, Transaction transaction)
            throws InsufficientFundsException {
        if (account.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds for fee debit"
            );
        }
        account.setBalance(account.getBalance().subtract(transaction.getAmount()));
    }

    private TransactionType getReverseTransactionType(TransactionType originalType) {
        switch (originalType) {
            case DEPOSIT:
                return TransactionType.WITHDRAWAL;
            case WITHDRAWAL:
                return TransactionType.DEPOSIT;
            case INTEREST_CREDIT:
                return TransactionType.FEE_DEBIT;
            case FEE_DEBIT:
                return TransactionType.INTEREST_CREDIT;
            default:
                return originalType;
        }
    }
}