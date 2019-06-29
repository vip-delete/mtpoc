package com.example.dao;

import com.example.model.Account;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class DataStoreImpl implements DataStore {
    private final AtomicLong idGenerator = new AtomicLong();
    private final Map<Long, AccountRef> accounts = new ConcurrentHashMap<>();

    @Override
    public long createAccount() {
        long id = idGenerator.incrementAndGet();
        accounts.put(id, new AccountRef());
        return id;
    }

    @Override
    public Account getAccount(long id) {
        AccountRef ref = accounts.get(id);
        return ref == null ? null : new Account(id, ref.getBalanceRef().get());
    }

    @Override
    public boolean update(long id, BigDecimal amount) {
        AccountRef ref = accounts.get(id);
        return ref != null && update(ref.getBalanceRef(), amount);
    }

    @Override
    public boolean transfer(long fromAccountId, long toAccountId, BigDecimal amount) {
        AccountRef from = accounts.get(fromAccountId);
        if (from == null) {
            // fromAccount not found
            return false;
        }

        AccountRef to = accounts.get(toAccountId);
        if (to == null) {
            // toAccount not found
            return false;
        }

        if (amount.signum() < 0) {
            // negative amount to transfer
            return false;
        }

        if (amount.signum() == 0) {
            // nothing changed
            return true;
        }

        if (fromAccountId == toAccountId) {
            // nothing changed
            return true;
        }

        if (!update(from.getBalanceRef(), amount.negate())) {
            // not enough money to transfer
            return false;
        }

        // (money is transferring here)

        // always returns true, because amount is positive
        update(to.getBalanceRef(), amount);

        // eventually consisted here
        return true;
    }

    // private

    private boolean update(AtomicReference<BigDecimal> balanceRef, BigDecimal amount) {
        boolean flag = false;
        while (!flag) {
            BigDecimal oldVal = balanceRef.get();
            BigDecimal newVal = oldVal.add(amount);
            if (amount.signum() < 0 && newVal.signum() < 0) {
                // we withdraw and got negative balance: overdraft not allowed
                return false;
            }
            flag = balanceRef.compareAndSet(oldVal, newVal);
        }
        return true;
    }
}
