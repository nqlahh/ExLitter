package com.splitapp.service.impl;

import com.splitapp.dto.DebtEntry;
import com.splitapp.model.Expense;
import com.splitapp.model.Group;
import com.splitapp.model.Member;
import com.splitapp.repository.ExpenseRepository;
import com.splitapp.repository.GroupRepository;
import com.splitapp.repository.MemberRepository;
import com.splitapp.service.DebtCalculatorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Single Responsibility: calculates net balances, simplified debts,
 * and records settlement payments. No UI or controller logic here.
 */
@Service
public class DebtCalculatorServiceImpl implements DebtCalculatorService {

    private final ExpenseRepository expenseRepository;
    private final MemberRepository  memberRepository;
    private final GroupRepository   groupRepository;

    public DebtCalculatorServiceImpl(ExpenseRepository expenseRepository,
                                     MemberRepository  memberRepository,
                                     GroupRepository   groupRepository) {
        this.expenseRepository = expenseRepository;
        this.memberRepository  = memberRepository;
        this.groupRepository   = groupRepository;
    }

    /**
     * Net balance per member name.
     * Positive  = member is owed money (paid more than their share).
     * Negative  = member owes money    (paid less than their share).
     */
    @Override
    public Map<String, BigDecimal> netBalances(Long groupId) {
        List<Member>  members  = memberRepository.findByGroupId(groupId);
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);

        // Keep insertion order so the UI is stable
        Map<String, BigDecimal> balance = new LinkedHashMap<>();
        for (Member m : members) balance.put(m.getName(), BigDecimal.ZERO);

        for (Expense expense : expenses) {
            List<Member> participants = expense.getParticipants();
            if (participants.isEmpty()) continue;

            BigDecimal share = expense.getTotalAmount().divide(
                BigDecimal.valueOf(participants.size()), 2, RoundingMode.HALF_UP);

            // Payer is credited the full amount
            balance.merge(expense.getPaidBy().getName(),
                          expense.getTotalAmount(), BigDecimal::add);

            // Each participant is debited their share
            for (Member p : participants) {
                balance.merge(p.getName(), share.negate(), BigDecimal::add);
            }
        }
        return balance;
    }

    /**
     * Debt-simplification algorithm:
     * Reduces the N×N debt matrix to the minimum number of transactions.
     * Now returns DebtEntry objects that also carry member IDs for the Settle button.
     */
    @Override
    public List<DebtEntry> calculateDebts(Long groupId) {
        List<Member>            members  = memberRepository.findByGroupId(groupId);
        Map<String, BigDecimal> balances = netBalances(groupId);

        // Build name → member map for ID lookup
        Map<String, Member> memberByName = new LinkedHashMap<>();
        for (Member m : members) memberByName.put(m.getName(), m);

        // Separate into creditors (positive) and debtors (negative)
        List<Map.Entry<String, BigDecimal>> creditors = new ArrayList<>();
        List<Map.Entry<String, BigDecimal>> debtors   = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> e : balances.entrySet()) {
            int cmp = e.getValue().compareTo(BigDecimal.ZERO);
            if (cmp > 0) creditors.add(new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()));
            if (cmp < 0) debtors.add(new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().negate()));
        }

        List<DebtEntry> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            String     dName   = debtors.get(i).getKey();
            String     cName   = creditors.get(j).getKey();
            BigDecimal owes    = debtors.get(i).getValue();
            BigDecimal owed    = creditors.get(j).getValue();
            BigDecimal payment = owes.min(owed);

            Long debtorId   = memberByName.containsKey(dName) ? memberByName.get(dName).getId() : null;
            Long creditorId = memberByName.containsKey(cName) ? memberByName.get(cName).getId() : null;

            result.add(new DebtEntry(dName, cName, payment, debtorId, creditorId));

            debtors.get(i).setValue(owes.subtract(payment));
            creditors.get(j).setValue(owed.subtract(payment));

            if (debtors.get(i).getValue().compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditors.get(j).getValue().compareTo(BigDecimal.ZERO) == 0) j++;
        }
        return result;
    }

    /**
     * Records a settlement: debtor pays `amount` to creditor.
     *
     * Implementation: we create a special expense where:
     *   - paidBy      = debtor  (debtor is now "paying")
     *   - participant = creditor only
     *   - amount      = settled amount
     *   - description = "Settlement: debtorName → creditorName"
     *
     * Net effect on balances:
     *   debtor   balance += amount   (they paid, so they get credited)
     *   creditor balance -= amount   (they received, so they are debited their own share)
     * This exactly cancels the debt by `amount`.
     */
    @Override
    @Transactional
    public void recordSettlement(Long groupId, Long debtorId, Long creditorId, BigDecimal amount) {
        Group  group    = groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        Member debtor   = memberRepository.findById(debtorId)
            .orElseThrow(() -> new IllegalArgumentException("Debtor not found"));
        Member creditor = memberRepository.findById(creditorId)
            .orElseThrow(() -> new IllegalArgumentException("Creditor not found"));

        Expense settlement = new Expense();
        settlement.setDescription("Settlement: " + debtor.getName() + " → " + creditor.getName());
        settlement.setTotalAmount(amount);
        settlement.setPaidBy(debtor);
        settlement.setGroup(group);
        settlement.setParticipants(List.of(creditor)); // only creditor is the "participant"

        expenseRepository.save(settlement);
    }
}