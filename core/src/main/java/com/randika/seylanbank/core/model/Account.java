package com.randika.seylanbank.core.model;

import jakarta.persistence.*;
import com.randika.seylanbank.core.enums.AccountType;
import com.randika.seylanbank.core.enums.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "minimum_balance", precision = 15, scale = 2)
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    // Constructors
    public Account() {}

    public Account(Customer customer, AccountType accountType) {
        this.customer = customer;
        this.accountType = accountType;
        this.interestRate = BigDecimal.valueOf(accountType.getDefaultInterestRate());
        this.createdDate = LocalDateTime.now();
        this.accountNumber = generateAccountNumber();
        setMinimumBalanceByType();
    }

    private String generateAccountNumber() {
        return accountType.name().substring(0, 3) + System.currentTimeMillis();
    }

    private void setMinimumBalanceByType() {
        switch (accountType) {
            case SAVINGS:
                this.minimumBalance = BigDecimal.valueOf(500);
                break;
            case CHECKING:
                this.minimumBalance = BigDecimal.valueOf(100);
                break;
            case FIXED_DEPOSIT:
                this.minimumBalance = BigDecimal.valueOf(10000);
                break;
            default:
                this.minimumBalance = BigDecimal.ZERO;
        }
    }

    public BigDecimal calculateInterest() {
        if (accountType == AccountType.CURRENT) {
            return BigDecimal.ZERO;
        }
        return balance.multiply(interestRate.divide(BigDecimal.valueOf(100)));
    }

    public boolean canWithdraw(BigDecimal amount) {
        BigDecimal afterWithdrawal = balance.subtract(amount);
        return afterWithdrawal.compareTo(minimumBalance) >= 0;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getMinimumBalance() { return minimumBalance; }
    public void setMinimumBalance(BigDecimal minimumBalance) { this.minimumBalance = minimumBalance; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
}