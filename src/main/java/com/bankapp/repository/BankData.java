package com.bankapp.repository;

import com.bankapp.model.Customer;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class BankData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<Customer> customers;
    private int nextAccountNumber;

    public BankData() {
        this(new ArrayList<>(), 1);
    }

    public BankData(List<Customer> customers, int nextAccountNumber) {
        this.customers = new ArrayList<>(customers);
        this.nextAccountNumber = nextAccountNumber;
    }

    public List<Customer> getCustomers() { return customers; }

    public int issueAccountNumber() {
        if (nextAccountNumber == Integer.MAX_VALUE) {
            throw new IllegalStateException("No more account numbers are available.");
        }
        return nextAccountNumber++;
    }
}
