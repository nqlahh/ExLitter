package com.splitapp.controller;

import com.splitapp.model.Group;
import com.splitapp.service.DebtCalculatorService;
import com.splitapp.service.ExpenseService;
import com.splitapp.service.GroupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/groups")
public class GroupController {

    private final GroupService           groupService;
    private final ExpenseService         expenseService;
    private final DebtCalculatorService  debtCalculatorService;

    // Constructor injection (DIP)
    public GroupController(GroupService groupService,
                           ExpenseService expenseService,
                           DebtCalculatorService debtCalculatorService) {
        this.groupService          = groupService;
        this.expenseService        = expenseService;
        this.debtCalculatorService = debtCalculatorService;
    }

    /** GET /groups — list all groups */
    @GetMapping
    public String listGroups(Model model) {
        List<Group> groups = groupService.findAll();

        // Build a net-balance summary per group for the badge display
        Map<Long, BigDecimal> netForCurrentUser = groups.stream()
            .collect(Collectors.toMap(
                Group::getId,
                g -> {
                    // Sum of all net balances — used as a group-level indicator
                    return debtCalculatorService.netBalances(g.getId()).values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                }
            ));

        model.addAttribute("groups", groups);
        model.addAttribute("netBalances", netForCurrentUser);
        return "groups/list";
    }

    /** POST /groups — create a new group */
    @PostMapping
    public String createGroup(@RequestParam("name") String name,
                              @RequestParam("members") String membersRaw) {
        List<String> memberNames = Arrays.stream(membersRaw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
        groupService.save(name, memberNames);
        return "redirect:/groups";
    }

    /** POST /groups/{id}/delete */
    @PostMapping("/{id}/delete")
    public String deleteGroup(@PathVariable Long id) {
        groupService.deleteById(id);
        return "redirect:/groups";
    }

    /** GET / — redirect to groups */
    @GetMapping("/")
    public String home() {
        return "redirect:/groups";
    }
}
