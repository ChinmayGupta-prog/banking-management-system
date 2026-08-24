package com.bankapp.model;

import java.io.Serializable;
import java.math.BigDecimal;

public enum AccountType implements Serializable {
    SAVINGS('S', new BigDecimal("5000.00")),
    CURRENT('C', new BigDecimal("10000.00"));

    private final char code;
    private final BigDecimal minimumBalance;

    AccountType(char code, BigDecimal minimumBalance) {
        this.code = code;
        this.minimumBalance = minimumBalance;
    }

    public char getCode() {
        return code;
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public static AccountType fromCode(char code) {
        return switch (Character.toUpperCase(code)) {
            case 'S' -> SAVINGS;
            case 'C' -> CURRENT;
            default -> throw new IllegalArgumentException("Account type must be S or C.");
        };
    }
}
