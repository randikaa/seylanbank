package com.randika.seylanbank.core.service;

import com.randika.seylanbank.core.model.Transaction;
import com.randika.seylanbank.core.enums.TransactionType;
import com.randika.seylanbank.core.exception.AccountNotFoundException;
import com.randika.seylanbank.core.exception.InsufficientFundsException;
import com.randika.seylanbank.core.exception.UnauthorizedAccessException;

import jakarta.ejb.Remote;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Remote
public interface TransactionService {
    Transaction processTransaction(Long accountId, TransactionType type, BigDecimal amount)
            throws AccountNotFoundException, InsufficientFundsException;

    void transferFunds(Long fromAccountId, Long toAccountId, BigDecimal amount)
            throws AccountNotFoundException, InsufficientFundsException;

    List<Transaction> getTransactionHistory(Long accountId, Date fromDate, Date toDate)
            throws AccountNotFoundException;

    void reverseTransaction(Long transactionId) throws Exception;

    void customerTransferFunds(Long fromAccountId, Long toAccountId, BigDecimal amount, Long customerId)
            throws AccountNotFoundException, InsufficientFundsException, UnauthorizedAccessException;

    List<Transaction> getMyTransactionHistory(Long accountId, Date fromDate, Date toDate, Long customerId)
            throws AccountNotFoundException, UnauthorizedAccessException;

    List<Transaction> getAllTransactions(Date fromDate, Date toDate);
    List<Transaction> getTransactionsByType(TransactionType type, Date fromDate, Date toDate);
    List<Transaction> getLargeTransactions(BigDecimal minimumAmount, Date fromDate, Date toDate);
}