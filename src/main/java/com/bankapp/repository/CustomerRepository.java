package com.bankapp.repository;

public interface CustomerRepository {
    BankData load();
    void save(BankData data);
}
