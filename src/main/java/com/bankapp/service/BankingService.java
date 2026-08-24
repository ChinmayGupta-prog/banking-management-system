package com.bankapp.service;

import com.bankapp.model.AccountType;
import com.bankapp.model.Customer;
import com.bankapp.repository.BankData;
import com.bankapp.repository.CustomerRepository;

import java.math.BigDecimal;
import java.util.List;

public final class BankingService {
    private final CustomerRepository repository;
    private final BankData data;

    public BankingService(CustomerRepository repository) {
        this.repository = repository;
        this.data = repository.load();
    }

    public Customer openAccount(String name, AccountType type, BigDecimal initialDeposit) {
        requirePositive(initialDeposit);
        if (initialDeposit.compareTo(type.getMinimumBalance()) < 0) {
            throw new IllegalArgumentException("Insufficient amount to open account.");
        }
        Customer customer = new Customer(data.issueAccountNumber(), name, type, initialDeposit);
        data.getCustomers().add(customer);
        repository.save(data);
        return customer;
    }

    public Customer deposit(int accountNumber, BigDecimal amount) {
        requirePositive(amount);
        Customer customer = findRequired(accountNumber);
        customer.deposit(amount);
        repository.save(data);
        return customer;
    }

    public Customer withdraw(int accountNumber, BigDecimal amount) {
        requirePositive(amount);
        Customer customer = findRequired(accountNumber);
        BigDecimal remaining = customer.getBalance().subtract(amount);
        if (remaining.compareTo(customer.getAccountType().getMinimumBalance()) < 0) {
            throw new IllegalArgumentException("Insufficient funds to maintain minimum balance ("
                    + customer.getAccountType().getMinimumBalance().toBigInteger() + ").");
        }
        customer.withdraw(amount);
        repository.save(data);
        return customer;
    }

    public Customer getAccount(int accountNumber) { return findRequired(accountNumber); }
    public List<Customer> getAllAccounts() { return List.copyOf(data.getCustomers()); }

    public void modifyAccount(int accountNumber, String newName) {
        findRequired(accountNumber).rename(newName);
        repository.save(data);
    }

    public void closeAccount(int accountNumber) {
        Customer customer = findRequired(accountNumber);
        data.getCustomers().remove(customer);
        repository.save(data);
    }

    private Customer findRequired(int accountNumber) {
        return data.getCustomers().stream()
                .filter(customer -> customer.getAccountNumber() == accountNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid account number."));
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
    }
}
