package com.randika.seylanbank.banking.validator;

import com.randika.seylanbank.core.constants.TransactionConstants;
import com.randika.seylanbank.core.model.Transaction;
import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.enums.TransactionType;
import com.randika.seylanbank.core.enums.AccountStatus;
import com.randika.seylanbank.core.exception.InvalidTransactionException;
import com.randika.seylanbank.core.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.util.logging.Logger;

public class TransactionValidator {

    private static final Logger LOGGER = Logger.getLogger(TransactionValidator.class.getName());

    public static void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new InvalidTransactionException("Transaction cannot be null");
        }

        validateAmount(transaction.getAmount());
        validateTransactionType(transaction.getType());
        validateAccount(transaction.getAccount());
    }

    public static void validateTransferRequest(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (fromAccount == null || toAccount == null) {
            throw new InvalidTransactionException("Source and destination accounts cannot be null");
        }

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }

        validateAmount(amount);
        validateAccountForDebit(fromAccount, amount);
        validateAccountForCredit(toAccount);
    }

    public static void validateAccountForDebit(Account account, BigDecimal amount) {
        validateAccount(account);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException("Account must be active for debit transactions");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            LOGGER.warning("Insufficient funds: Account " + account.getAccountNumber() +
                    ", Balance: " + account.getBalance() + ", Required: " + amount);
            throw new InsufficientFundsException("Insufficient funds in account: ");
        }

        // Check minimum balance requirements
        if (account.getMinimumBalance() != null) {
            BigDecimal balanceAfterTransaction = account.getBalance().subtract(amount);
            if (balanceAfterTransaction.compareTo(account.getMinimumBalance()) < 0) {
                throw new InvalidTransactionException("Transaction would violate minimum balance requirement");
            }
        }
    }

    public static void validateAccountForCredit(Account account) {
        validateAccount(account);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new InvalidTransactionException("Cannot credit to a closed account");
        }
    }

    public static void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidTransactionException("Transaction amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transaction amount must be positive");
        }

        if (amount.compareTo(TransactionConstants.MAX_TRANSACTION_AMOUNT) > 0) {
            throw new InvalidTransactionException("Transaction amount exceeds maximum limit");
        }
    }

    public static void validateTransactionType(TransactionType type) {
        if (type == null) {
            throw new InvalidTransactionException("Transaction type cannot be null");
        }
    }

    public static void validateAccount(Account account) {
        if (account == null) {
            throw new InvalidTransactionException("Account cannot be null");
        }

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new InvalidTransactionException("Cannot perform transaction on closed account");
        }

        if (account.getStatus() == AccountStatus.SUSPENDED) {
            throw new InvalidTransactionException("Account is suspended");
        }
    }

    public static void validateDailyTransactionLimit(Account account, BigDecimal amount) {
        // Implementation would check daily transaction limits
        // This is a placeholder for more complex validation logic
        LOGGER.info("Validating daily transaction limit for account: " + account.getAccountNumber());
    }

    public static void validateBusinessHours() {
        // Implementation would check if transactions are allowed at current time
        LOGGER.info("Validating business hours for transaction");
    }
}