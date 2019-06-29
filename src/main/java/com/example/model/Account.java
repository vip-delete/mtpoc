package com.example.model;

import java.math.BigDecimal;

public class Account {
    private final long id;
    private final BigDecimal balance;

    public Account(long id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    // Getters

    public long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
