package com.example.dao;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

class AccountRef {
    private final AtomicReference<BigDecimal> balanceRef = new AtomicReference<>(BigDecimal.ZERO);

    AtomicReference<BigDecimal> getBalanceRef() {
        return balanceRef;
    }
}
