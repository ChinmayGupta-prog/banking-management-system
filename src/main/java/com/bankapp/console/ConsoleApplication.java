package com.bankapp.console;

import com.bankapp.model.AccountType;
import com.bankapp.model.Customer;
import com.bankapp.repository.DataAccessException;
import com.bankapp.service.BankingService;

import java.io.Console;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

public final class ConsoleApplication {
    private static final String PASSWORD_ENV = "BANK_APP_PASSWORD";
    private final BankingService service;
    private final Map<String, String> environment;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleApplication(BankingService service, Map<String, String> environment) {
        this.service = service;
        this.environment = environment;
    }

    public void run() {
        System.out.println("\n*** WELCOME TO THE BANKING MANAGEMENT SYSTEM ***\n");
        if (!authenticate()) return;
        System.out.println("\n                   Welcome to Bank Management System\n");

        int choice;
        do {
            printMenu();
            choice = readInt("Enter your choice: ");
            try {
                handle(choice);
            } catch (IllegalArgumentException | DataAccessException e) {
                System.out.println(e.getMessage());
            }
            if (choice != 8) pause();
        } while (choice != 8);
    }

    private boolean authenticate() {
        String configuredPassword = environment.get(PASSWORD_ENV);
        if (configuredPassword == null || configuredPassword.isBlank()) {
            System.out.println(PASSWORD_ENV + " is not configured. Set it before starting the application.");
            return false;
        }
        Console console = System.console();
        if (console == null) {
            System.out.println("No console available. Run the application from a system terminal.");
            return false;
        }
        char[] entered = console.readPassword("Enter your password: ");
        char[] expected = configuredPassword.toCharArray();
        boolean authenticated = Arrays.equals(entered, expected);
        Arrays.fill(entered, '\0');
        Arrays.fill(expected, '\0');
        if (!authenticated) System.out.println("You are not an authorized user.");
        return authenticated;
    }

    private void handle(int choice) {
        switch (choice) {
            case 1 -> openAccount();
            case 2 -> deposit();
            case 3 -> withdraw();
            case 4 -> showBalance();
            case 5 -> showAll();
            case 6 -> modifyAccount();
            case 7 -> closeAccount();
            case 8 -> System.out.println("Goodbye!");
            default -> System.out.println("Incorrect input.");
        }
    }

    private void openAccount() {
        header("Open New Account");
        String name = readRequiredLine("Enter name: ");
        AccountType type = readAccountType();
        BigDecimal minimum = type.getMinimumBalance();
        BigDecimal initial = readAmount("Enter initial deposit (min " + minimum.toBigInteger() + "): ");
        Customer customer = service.openAccount(name, type, initial);
        System.out.println("Account created. Your account number is " + customer.getAccountNumber());
    }

    private void deposit() {
        header("Deposit");
        Customer customer = service.deposit(readInt("Enter account number: "), readAmount("Enter amount to deposit: "));
        System.out.printf("Deposited. New balance: %.2f%n", customer.getBalance());
    }

    private void withdraw() {
        header("Withdraw");
        Customer customer = service.withdraw(readInt("Enter account number: "), readAmount("Enter amount to withdraw: "));
        System.out.printf("Withdrawn. New balance: %.2f%n", customer.getBalance());
    }

    private void showBalance() {
        header("Show Balance");
        Customer customer = service.getAccount(readInt("Enter account number: "));
        System.out.printf("Balance for A/C %d (%s): %.2f%n", customer.getAccountNumber(), customer.getName(), customer.getBalance());
    }

    private void showAll() {
        header("All Accounts");
        var customers = service.getAllAccounts();
        if (customers.isEmpty()) { System.out.println("No accounts found."); return; }
        System.out.printf("%-6s %-20s %-1s %10s%n", "Acno", "Name", "T", "Balance");
        customers.forEach(System.out::println);
    }

    private void modifyAccount() {
        header("Modify Account");
        int accountNumber = readInt("Enter account number: ");
        Customer current = service.getAccount(accountNumber);
        String newName = readLine("Enter new name (leave blank to keep '" + current.getName() + "'): ");
        if (!newName.isEmpty()) service.modifyAccount(accountNumber, newName);
        System.out.println("Account updated.");
    }

    private void closeAccount() {
        header("Close Account");
        service.closeAccount(readInt("Enter account number: "));
        System.out.println("Account closed successfully.");
    }

    private void printMenu() {
        System.out.println("\nMenu\n1. Open New Account\n2. Deposit\n3. Withdraw\n4. Show Balance\n5. Show All\n6. Modify Account\n7. Close Account\n8. Exit");
    }

    private void pause() { readLine("\nPress ENTER to continue..."); }
    private void header(String title) { System.out.println("\n==================== " + title + " ===================="); }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readRequiredLine(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) return value;
            System.out.println("Input cannot be blank.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                int value = Integer.parseInt(readLine(prompt));
                if (value > 0) return value;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a valid positive integer.");
        }
    }

    private BigDecimal readAmount(String prompt) {
        while (true) {
            try {
                BigDecimal value = new BigDecimal(readLine(prompt));
                if (value.compareTo(BigDecimal.ZERO) > 0) return value;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a valid positive amount.");
        }
    }

    private AccountType readAccountType() {
        while (true) {
            String value = readLine("Enter S for Saving or C for Current: ").toUpperCase();
            if (value.length() == 1) {
                try { return AccountType.fromCode(value.charAt(0)); } catch (IllegalArgumentException ignored) { }
            }
            System.out.println("Invalid input. Try again.");
        }
    }
}
