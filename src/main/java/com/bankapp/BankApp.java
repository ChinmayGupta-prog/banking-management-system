package com.bankapp;

import com.bankapp.console.ConsoleApplication;
import com.bankapp.repository.DataAccessException;
import com.bankapp.repository.SerializationCustomerRepository;
import com.bankapp.service.BankingService;

import java.nio.file.Path;

public final class BankApp {
    private static final Path DATA_FILE = Path.of("bank.dat");

    private BankApp() {
    }

    public static void main(String[] args) {
        try {
            var repository = new SerializationCustomerRepository(DATA_FILE);
            var service = new BankingService(repository);
            new ConsoleApplication(service, System.getenv()).run();
        } catch (DataAccessException e) {
            System.err.println(e.getMessage());
            System.err.println("The existing data file was left unchanged. Restore or remove it before retrying.");
        }
    }
}
