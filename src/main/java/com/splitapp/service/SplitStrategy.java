package com.splitapp.service;

import com.splitapp.model.Member;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Strategy interface for expense splitting (Open/Closed Principle).
 * New split types (percentage, exact amounts) implement this without
 * modifying existing code.
 */
public interface SplitStrategy {
    /**
     * @param totalAmount  total expense amount
     * @param participants members sharing this expense
     * @return map of member → their share amount
     */
    Map<Member, BigDecimal> calculateSplit(BigDecimal totalAmount, List<Member> participants);
}
