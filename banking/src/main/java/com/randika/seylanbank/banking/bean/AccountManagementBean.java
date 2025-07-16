package com.randika.seylanbank.banking.bean;

import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;

import com.randika.seylanbank.core.service.AccountService;
import com.randika.seylanbank.core.model.*;
import com.randika.seylanbank.core.enums.*;
import com.randika.seylanbank.core.exception.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AccountManagementBean implements AccountService {

    @PersistenceContext(unitName = "SeylanBankPU")
    private EntityManager em;

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public Account createAccount(Customer customer, AccountType type) {
        Account account = new Account(customer, type);
        em.persist(account);
        return account;
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "CUSTOMER"})
    public void updateAccount(Account account) {
        em.merge(account);
    }

    @Override
    @PermitAll
    public Account findAccountById(Long id) throws AccountNotFoundException {
        Account account = em.find(Account.class, id);
        if (account == null) {
            throw new AccountNotFoundException("Account not found with ID: " + id);
        }
        return account;
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN", "CUSTOMER"})
    public List<Account> findAccountsByCustomer(Long customerId) {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.customer.id = :customerId", Account.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void closeAccount(Long accountId) throws AccountNotFoundException {
        Account account = findAccountById(accountId);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance");
        }
        account.setStatus(AccountStatus.CLOSED);
        em.merge(account);
    }

    @Override
    @PermitAll
    public BigDecimal getAccountBalance(Long accountId) throws AccountNotFoundException {
        Account account = findAccountById(accountId);
        return account.getBalance();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Account> findAccountsByType(AccountType type) {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.accountType = :type", Account.class);
        query.setParameter("type", type);
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Account> findAllAccounts() {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a ORDER BY a.createdDate DESC", Account.class);
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void suspendAccount(Long accountId) throws AccountNotFoundException {
        Account account = findAccountById(accountId);
        account.setStatus(AccountStatus.SUSPENDED);
        em.merge(account);
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public void activateAccount(Long accountId) throws AccountNotFoundException {
        Account account = findAccountById(accountId);
        account.setStatus(AccountStatus.ACTIVE);
        em.merge(account);
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Account> findInactiveAccounts() {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.status = :status", Account.class);
        query.setParameter("status", AccountStatus.INACTIVE);
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Account> findAccountsWithLowBalance(BigDecimal threshold) {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.balance < :threshold AND a.status = :status", Account.class);
        query.setParameter("threshold", threshold);
        query.setParameter("status", AccountStatus.ACTIVE);
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"CUSTOMER"})
    public List<Account> findMyAccounts(Long customerId) {
        return findAccountsByCustomer(customerId);
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Account> findByAccountNumber(String accountNumber) {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.accountNumber LIKE :accNo", Account.class);
        query.setParameter("accNo", "%" + accountNumber + "%");
        return query.getResultList();
    }

    @Override
    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    public List<Account> findByCustomerName(String customerName) {
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE LOWER(a.customer.fullName) LIKE :name", Account.class);
        query.setParameter("name", "%" + customerName.toLowerCase() + "%");
        return query.getResultList();
    }

}