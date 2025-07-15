package com.randika.seylanbank.banking.validator;

import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.model.Customer;
import com.randika.seylanbank.core.enums.AccountType;
import com.randika.seylanbank.core.enums.AccountStatus;
import com.randika.seylanbank.core.exception.InvalidTransactionException;
import com.randika.seylanbank.core.constants.AccountConstants;

import java.math.BigDecimal;
import java.util.logging.Logger;

public class AccountValidator {

    private static final Logger LOGGER = Logger.getLogger(AccountValidator.class.getName());

    public static void validateAccountCreation(Customer customer, AccountType accountType) {
        if (customer == null) {
            throw new InvalidTransactionException("Customer cannot be null");
        }

        if (accountType == null) {
            throw new InvalidTransactionException("Account type cannot be null");
        }

        validateCustomer(customer);
    }

    public static void validateAccount(Account account) {
        if (account == null) {
            throw new InvalidTransactionException("Account cannot be null");
        }

        validateAccountNumber(account.getAccountNumber());
        validateAccountStatus(account.getStatus());
        validateAccountBalance(account.getBalance());
    }

    public static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new InvalidTransactionException("Account number cannot be null or empty");
        }

        if (accountNumber.length() != AccountConstants.ACCOUNT_NUMBER_LENGTH) {
            throw new InvalidTransactionException("Account number must be " +
                    AccountConstants.ACCOUNT_NUMBER_LENGTH + " digits");
        }

        if (!accountNumber.matches("\\d{" + AccountConstants.ACCOUNT_NUMBER_LENGTH + "}")) {
            throw new InvalidTransactionException("Account number must contain only digits");
        }
    }

    public static void validateAccountStatus(AccountStatus status) {
        if (status == null) {
            throw new InvalidTransactionException("Account status cannot be null");
        }
    }

    public static void validateAccountBalance(BigDecimal balance) {
        if (balance == null) {
            throw new InvalidTransactionException("Account balance cannot be null");
        }

        if (balance.compareTo(AccountConstants.MINIMUM_ACCOUNT_BALANCE) < 0) {
            throw new InvalidTransactionException("Account balance cannot be negative beyond minimum limit");
        }
    }

    public static void validateCustomer(Customer customer) {
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            throw new InvalidTransactionException("Customer first name cannot be null or empty");
        }

        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            throw new InvalidTransactionException("Customer last name cannot be null or empty");
        }

        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new InvalidTransactionException("Customer email cannot be null or empty");
        }

        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            throw new InvalidTransactionException("Customer phone number cannot be null or empty");
        }
    }

    public static void validateAccountUpdate(Account account, Account updatedAccount) {
        if (account == null || updatedAccount == null) {
            throw new InvalidTransactionException("Accounts cannot be null for update");
        }

        if (!account.getId().equals(updatedAccount.getId())) {
            throw new InvalidTransactionException("Account IDs must match for update");
        }

        // Validate that critical fields are not changed inappropriately
        if (!account.getAccountNumber().equals(updatedAccount.getAccountNumber())) {
            throw new InvalidTransactionException("Account number cannot be changed");
        }

        if (!account.getCustomer().getId().equals(updatedAccount.getCustomer().getId())) {
            throw new InvalidTransactionException("Account customer cannot be changed");
        }
    }

    public static void validateAccountClosure(Account account) {
        validateAccount(account);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new InvalidTransactionException("Account is already closed");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidTransactionException("Account must have zero balance before closure");
        }
    }
}