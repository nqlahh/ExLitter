package com.splitapp.controller;

import com.splitapp.dto.DebtEntry;
import com.splitapp.model.Group;
import com.splitapp.service.DebtCalculatorService;
import com.splitapp.service.GroupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/groups/{groupId}/settle")
public class SettleController {

    private final GroupService          groupService;
    private final DebtCalculatorService debtCalculatorService;

    public SettleController(GroupService groupService,
                            DebtCalculatorService debtCalculatorService) {
        this.groupService          = groupService;
        this.debtCalculatorService = debtCalculatorService;
    }

    @GetMapping
    public String settleUp(@PathVariable Long groupId, Model model) {
        Group group = groupService.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        // 🟢 THE CRITICAL CHANGE: Switch calculation hook from calculateDebts() to calculateDirectDebts()
        List<DebtEntry>         debts    = debtCalculatorService.calculateDirectDebts(groupId);
        Map<String, BigDecimal> balances = debtCalculatorService.netBalances(groupId);

        // Pre-build per-member owes/receives lists in the controller
        // so the template loops remain perfectly simple with zero SpEL requirements
        Map<String, List<DebtEntry>> owesMap    = new LinkedHashMap<>();
        Map<String, List<DebtEntry>> receivesMap = new LinkedHashMap<>();

        for (String name : balances.keySet()) {
            owesMap.put(name,     new ArrayList<>());
            receivesMap.put(name, new ArrayList<>());
        }
        
        for (DebtEntry d : debts) {
            if (owesMap.containsKey(d.getDebtor()))       owesMap.get(d.getDebtor()).add(d);
            if (receivesMap.containsKey(d.getCreditor())) receivesMap.get(d.getCreditor()).add(d);
        }

        BigDecimal totalOwed = balances.values().stream()
            .filter(v -> v.compareTo(BigDecimal.ZERO) > 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOwe = balances.values().stream()
            .filter(v -> v.compareTo(BigDecimal.ZERO) < 0)
            .map(BigDecimal::negate)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("group",       group);
        model.addAttribute("debts",       debts);
        model.addAttribute("balances",    balances);
        model.addAttribute("owesMap",     owesMap);
        model.addAttribute("receivesMap", receivesMap);
        model.addAttribute("totalOwed",   totalOwed);
        model.addAttribute("totalOwe",    totalOwe);
        model.addAttribute("net",         totalOwed.subtract(totalOwe));
        return "settle/index";
    }

    @PostMapping("/partial")
    public String partialSettle(@PathVariable Long groupId,
                                @RequestParam("debtorId")   Long debtorId,
                                @RequestParam("creditorId") Long creditorId,
                                @RequestParam("amount")     BigDecimal amount) {
        debtCalculatorService.recordSettlement(groupId, debtorId, creditorId, amount);
        return "redirect:/groups/" + groupId + "/settle";
    }
}