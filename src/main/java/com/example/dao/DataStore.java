package com.example.dao;

import com.example.model.Account;

import java.math.BigDecimal;

/**
 * Simple DataStore.
 */
public interface DataStore {
    /**
     * Create Account
     *
     * @return account id
     */
    long createAccount();

    /**
     * Get Account by id
     *
     * @param id account id
     * @return account or null of not found
     */
    Account getAccount(long id);

    /**
     * Add/Withdraw money
     * Overdraft is not allowed
     *
     * @param id     account id
     * @param amount amount to add (amount is positive) or withdraw (amount is negative)
     * @return true if successful, false otherwise
     */
    boolean update(long id, BigDecimal amount);

    /**
     * Transfer money between accounts
     * Overdraft is not allowed
     *
     * @param fromAccountId account id to transfer money from
     * @param toAccountId   account id to transfer money to
     * @param amount        amount, negative is not allowed
     * @return true if successful, false otherwise
     */
    boolean transfer(long fromAccountId, long toAccountId, BigDecimal amount);
}
