package com.splitapp.service;

import com.splitapp.model.Expense;
import java.math.BigDecimal;
import java.util.List;

public interface ExpenseService {
    List<Expense> findByGroupId(Long groupId);
    Expense addExpense(Long groupId, String description, BigDecimal amount,
                       Long paidByMemberId, List<Long> participantIds);
    void deleteById(Long id);
    BigDecimal totalSpentByGroup(Long groupId);
}
