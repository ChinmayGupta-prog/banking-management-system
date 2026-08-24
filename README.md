# Banking Management System

A menu-driven Core Java application for creating and managing savings and current bank accounts in a local terminal.

## Stack

- Java 17
- Maven 3.8+
- Java object serialization for local file persistence
- Standard Java console APIs; no framework, database, REST API, or web interface

## Features

- Create savings and current accounts with automatically assigned account numbers
- Enforce account-type minimum opening and remaining balances
- Deposit and withdraw money, with positive-amount validation
- View one balance or list every account
- Modify an account holder's name and close an account
- Authenticate the console using a password supplied through an environment variable
- Persist account data between runs in `bank.dat`
- Reject blank names, unsupported account types, invalid numbers, and non-positive amounts
- Report corrupt or incompatible persistence data without silently overwriting it

## Architecture

The entry point wires a terminal UI to a service layer and a serialization repository. The console owns input/output, the service owns banking rules, model classes own account state, and the repository owns disk access.

```text
BankApp -> ConsoleApplication -> BankingService -> CustomerRepository
                                  |                    |
                           Customer/AccountType   bank.dat
```

### Class responsibilities

| Class | Responsibility |
| --- | --- |
| `BankApp` | Creates dependencies and starts the application. |
| `ConsoleApplication` | Authenticates the operator, displays the menu, validates terminal input, and prints results. |
| `BankingService` | Applies account-opening, deposit, withdrawal, lookup, modification, and closure rules. |
| `Customer` | Represents one account holder, account type, account number, and monetary balance. |
| `AccountType` | Defines savings/current type codes and their minimum balances. |
| `CustomerRepository` | Defines the persistence boundary. |
| `SerializationCustomerRepository` | Loads and atomically saves serialized application data. |
| `BankData` | Stores customers and the next account number so closed numbers are not reused. |
| `DataAccessException` | Reports persistence failures to the application boundary. |

## Account operations

| Operation | Input | Rule/result |
| --- | --- | --- |
| Open account | Name, type, initial deposit | Name must not be blank; deposit must meet the type's minimum; a unique sequential number is assigned. |
| Deposit | Account number, amount | Account must exist and amount must be positive. |
| Withdraw | Account number, amount | Account must exist, amount must be positive, and the remaining balance must meet the minimum. |
| Show balance | Account number | Prints the matching holder and balance. |
| Show all | None | Prints all accounts currently stored. |
| Modify | Account number, new name | Changes only the holder name; a blank entry keeps the current name. |
| Close | Account number | Removes the account; no transfer or payout workflow is implemented. |

## Minimum-balance rules

| Account type | Code | Minimum opening and remaining balance |
| --- | --- | ---: |
| Savings | `S` | 5,000.00 |
| Current | `C` | 10,000.00 |

Money is represented with `BigDecimal` and stored at two decimal places using half-even rounding. This avoids the binary floating-point errors associated with `double`.

## Local setup

Prerequisites:

- JDK 17 or newer (`java -version`)
- Maven 3.8 or newer (`mvn -version`)
- A real system terminal. Password entry uses `System.console()` so the password is not echoed.

From a clean clone, open PowerShell in the repository directory and run:

```powershell
$env:BANK_APP_PASSWORD = "choose-a-local-password"
mvn clean package
java -jar target/banking-management-system-1.0.0.jar
```

For Command Prompt:

```bat
set BANK_APP_PASSWORD=choose-a-local-password
mvn clean package
java -jar target\banking-management-system-1.0.0.jar
```

For macOS or Linux:

```bash
export BANK_APP_PASSWORD='choose-a-local-password'
mvn clean package
java -jar target/banking-management-system-1.0.0.jar
```

The application deliberately has no default password. Set `BANK_APP_PASSWORD` in each new shell or through your operating system's environment settings. Do not commit a real password.

To compile without producing the packaged JAR, run `mvn clean compile`. The compiled classes are written to `target/classes`.

## Data persistence

The application creates `bank.dat` in the working directory after the first change. It contains a Java-serialized `BankData` object with the accounts and next account number. Saves are written to `bank.dat.tmp` and then moved into place, reducing the chance of a partially written main file. Both files are ignored by Git.

Serialization is local persistence, not encryption or a database. Do not edit `bank.dat`, expose it to untrusted input, or commit it. If the file is empty, corrupt, or from an incompatible model version, startup stops with an error and leaves the file unchanged. Delete it only when intentionally starting with empty data; back it up if the records matter.

## Example console workflow

```text
*** WELCOME TO THE BANKING MANAGEMENT SYSTEM ***

Enter your password:

Menu
1. Open New Account
...
Enter your choice: 1

==================== Open New Account ====================
Enter name: Asha Rao
Enter S for Saving or C for Current: S
Enter initial deposit (min 5000): 7500
Account created. Your account number is 1

Enter your choice: 2
Enter account number: 1
Enter amount to deposit: 500
Deposited. New balance: 8000.00
```

## Design decisions

- **Layered packages instead of one large class:** model, repository, service, and console responsibilities are separated so business rules are not coupled to terminal formatting or file I/O.
- **`BigDecimal` instead of `double`:** decimal arithmetic is appropriate for currency and produces predictable two-decimal balances.
- **Serialized local state instead of a database:** serialization preserves the original small, offline console-project scope and requires no external service. A relational database was rejected because the project has no concurrent users, server process, or query requirements.
- **A persisted account-number counter instead of `max + 1`:** numbers continue increasing even if the latest account is closed, preventing accidental number reuse.
- **Environment configuration instead of a source-code password:** the shared console password is kept out of Git. A command-line password was rejected because it can appear in shell history and process listings.
- **Hidden terminal input instead of ordinary scanner input:** `System.console().readPassword()` avoids echoing the password. The application fails closed when no real console is available.
- **Atomic replacement instead of writing directly to `bank.dat`:** a temporary file reduces partial-write risk. Corrupt or incompatible input causes startup to stop rather than treating it as empty data and later overwriting it.
- **Maven without runtime dependencies:** Maven provides a repeatable layout and build while the application remains Core Java.

## Scope

This is a single-process learning project. It does not implement transfers, transaction history, interest calculation, per-customer login, password hashing, encryption, concurrency control, a database, REST endpoints, a web UI, or automated tests.
