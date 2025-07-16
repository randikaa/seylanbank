package com.randika.seylanbank.core.service;

import com.randika.seylanbank.core.enums.AccountType;
import com.randika.seylanbank.core.exception.AccountNotFoundException;
import com.randika.seylanbank.core.model.Account;
import com.randika.seylanbank.core.model.Customer;
import jakarta.ejb.Remote;

import java.math.BigDecimal;
import java.util.List;

@Remote
public interface AccountService {
    Account createAccount(Customer customer, AccountType type);
    void updateAccount(Account account);
    Account findAccountById(Long id);
    List<Account> findAccountsByCustomer(Long customerId);
    void closeAccount(Long accountId);
    BigDecimal getAccountBalance(Long accountId);

    List<Account> findAccountsByType(AccountType type);
    List<Account> findAllAccounts();
    void suspendAccount(Long accountId) throws AccountNotFoundException;
    void activateAccount(Long accountId) throws AccountNotFoundException;
    List<Account> findInactiveAccounts();
    List<Account> findAccountsWithLowBalance(BigDecimal threshold);
    List<Account> findMyAccounts(Long customerId);

    List<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomerName(String customerName);

}
