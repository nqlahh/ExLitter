package com.splitapp.service;

import com.splitapp.dto.DebtEntry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Single-responsibility: calculates and records debts for a group.
 */
public interface DebtCalculatorService {

    /** Simplified list of who owes whom using the Greedy Two-Pointer Algorithm. */
    List<DebtEntry> calculateDebts(Long groupId);

    /** Raw, un-simplified list of exact direct transaction lines for the dashboard UI. */
    List<DebtEntry> calculateDirectDebts(Long groupId);

    /** Net balance per member name: positive = owed money, negative = owes money */
    Map<String, BigDecimal> netBalances(Long groupId);

    /**
     * Records a settlement payment: debtor pays `amount` to creditor.
     * This creates a settlement expense that reduces the outstanding balance.
     */
    void recordSettlement(Long groupId, Long debtorId, Long creditorId, BigDecimal amount);
}