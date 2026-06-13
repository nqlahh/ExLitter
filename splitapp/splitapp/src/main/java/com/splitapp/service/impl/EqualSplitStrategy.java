package com.splitapp.service.impl;

import com.splitapp.model.Member;
import com.splitapp.service.SplitStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Splits the total amount equally among all participants.
 * Adding a new strategy (e.g. PercentageSplitStrategy) does not
 * require modifying this class (Open/Closed Principle).
 */
@Component
public class EqualSplitStrategy implements SplitStrategy {

    @Override
    public Map<Member, BigDecimal> calculateSplit(BigDecimal totalAmount, List<Member> participants) {
        Map<Member, BigDecimal> result = new LinkedHashMap<>();
        if (participants == null || participants.isEmpty()) return result;

        BigDecimal share = totalAmount.divide(
            BigDecimal.valueOf(participants.size()),
            2, RoundingMode.HALF_UP
        );

        for (Member m : participants) {
            result.put(m, share);
        }
        return result;
    }
}
