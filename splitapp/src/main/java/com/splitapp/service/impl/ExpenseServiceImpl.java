package com.splitapp.service.impl;

import com.splitapp.model.Expense;
import com.splitapp.model.Group;
import com.splitapp.model.Member;
import com.splitapp.repository.ExpenseRepository;
import com.splitapp.repository.GroupRepository;
import com.splitapp.repository.MemberRepository;
import com.splitapp.service.ExpenseService;
import com.splitapp.service.SplitStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository   groupRepository;
    private final MemberRepository  memberRepository;
    private final SplitStrategy     splitStrategy;   // DIP: injected, not instantiated

    public ExpenseServiceImpl(ExpenseRepository expenseRepository,
                              GroupRepository groupRepository,
                              MemberRepository memberRepository,
                              SplitStrategy splitStrategy) {
        this.expenseRepository = expenseRepository;
        this.groupRepository   = groupRepository;
        this.memberRepository  = memberRepository;
        this.splitStrategy     = splitStrategy;
    }

    @Override
    public List<Expense> findByGroupId(Long groupId) {
        return expenseRepository.findByGroupId(groupId);
    }

    @Override
    public Expense addExpense(Long groupId, String description, BigDecimal amount,
                              Long paidByMemberId, List<Long> participantIds) {

        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        Member paidBy = memberRepository.findById(paidByMemberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + paidByMemberId));

        List<Member> participants;
        if (participantIds == null || participantIds.isEmpty()) {
            // Default: split among all group members
            participants = memberRepository.findByGroupId(groupId);
        } else {
            participants = memberRepository.findAllById(participantIds);
        }

        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setTotalAmount(amount);
        expense.setPaidBy(paidBy);
        expense.setGroup(group);
        expense.setParticipants(participants);

        // Strategy pattern: splitStrategy.calculateSplit() is called here;
        // swapping to a different strategy requires no changes in this class (OCP)
        splitStrategy.calculateSplit(amount, participants);

        return expenseRepository.save(expense);
    }

    @Override
    public void deleteById(Long id) {
        expenseRepository.deleteById(id);
    }

    @Override
    public BigDecimal totalSpentByGroup(Long groupId) {
        return expenseRepository.findByGroupId(groupId).stream()
            .map(Expense::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
