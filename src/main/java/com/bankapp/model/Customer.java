package com.bankapp.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Customer implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int accountNumber;
    private String name;
    private final AccountType accountType;
    private BigDecimal balance;

    public Customer(int accountNumber, String name, AccountType accountType, BigDecimal balance) {
        if (accountNumber <= 0) throw new IllegalArgumentException("Account number must be positive.");
        this.accountNumber = accountNumber;
        this.name = requireName(name);
        this.accountType = Objects.requireNonNull(accountType, "Account type is required.");
        this.balance = normalize(balance);
    }

    public int getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public AccountType getAccountType() { return accountType; }
    public BigDecimal getBalance() { return balance; }

    public void rename(String newName) { this.name = requireName(newName); }
    public void deposit(BigDecimal amount) { balance = balance.add(normalize(amount)); }
    public void withdraw(BigDecimal amount) { balance = balance.subtract(normalize(amount)); }

    private static String requireName(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be blank.");
        return value.trim();
    }

    private static BigDecimal normalize(BigDecimal value) {
        Objects.requireNonNull(value, "Amount is required.");
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public String toString() {
        return String.format("%-6d %-20s %-1s %10.2f", accountNumber, name,
                accountType.getCode(), balance);
    }
}
