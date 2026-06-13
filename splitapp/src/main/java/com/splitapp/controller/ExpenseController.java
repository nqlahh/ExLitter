package com.splitapp.controller;

import com.splitapp.model.Expense;
import com.splitapp.model.Group;
import com.splitapp.model.Member;
import com.splitapp.repository.MemberRepository;
import com.splitapp.service.ExpenseService;
import com.splitapp.service.GroupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService   expenseService;
    private final GroupService     groupService;
    private final MemberRepository memberRepository;

    public ExpenseController(ExpenseService expenseService,
                             GroupService groupService,
                             MemberRepository memberRepository) {
        this.expenseService   = expenseService;
        this.groupService     = groupService;
        this.memberRepository = memberRepository;
    }

    /** GET /groups/{groupId}/expenses */
    @GetMapping
    public String listExpenses(@PathVariable Long groupId, Model model) {
        Group group = groupService.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        List<Expense> expenses = expenseService.findByGroupId(groupId);
        BigDecimal total       = expenseService.totalSpentByGroup(groupId);

        model.addAttribute("group",    group);
        model.addAttribute("expenses", expenses);
        model.addAttribute("total",    total);
        model.addAttribute("members",  group.getMembers());
        return "expenses/list";
    }

    /** POST /groups/{groupId}/expenses — add a new expense */
    @PostMapping
    public String addExpense(@PathVariable Long groupId,
                             @RequestParam("description")   String description,
                             @RequestParam("totalAmount")   BigDecimal totalAmount,
                             @RequestParam("paidByMemberId") Long paidByMemberId,
                             @RequestParam(value = "participantIds", required = false)
                                 List<Long> participantIds) {

        // If no specific participants selected, default to all (equal split)
        List<Long> participants = (participantIds != null && !participantIds.isEmpty())
            ? participantIds
            : Collections.emptyList();

        expenseService.addExpense(groupId, description, totalAmount, paidByMemberId, participants);
        return "redirect:/groups/" + groupId + "/expenses";
    }

    /** POST /groups/{groupId}/expenses/{expenseId}/delete */
    @PostMapping("/{expenseId}/delete")
    public String deleteExpense(@PathVariable Long groupId,
                                @PathVariable Long expenseId) {
        expenseService.deleteById(expenseId);
        return "redirect:/groups/" + groupId + "/expenses";
    }
}
