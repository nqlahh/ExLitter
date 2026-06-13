package com.splitapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** The member who paid for this expense */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_member_id", nullable = false)
    private Member paidBy;

    /** The group this expense belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    /** Which members are participating in this split */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "expense_participants",
        joinColumns        = @JoinColumn(name = "expense_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<Member> participants = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────────────────────
    public Expense() {}

    // ── Helpers ───────────────────────────────────────────────────────────────
    /** Each participant's equal share */
    public BigDecimal sharePerParticipant() {
        if (participants.isEmpty()) return BigDecimal.ZERO;
        return totalAmount.divide(
            BigDecimal.valueOf(participants.size()),
            2, java.math.RoundingMode.HALF_UP
        );
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }

    public BigDecimal getTotalAmount()         { return totalAmount; }
    public void setTotalAmount(BigDecimal a)   { this.totalAmount = a; }

    public Member getPaidBy()                  { return paidBy; }
    public void setPaidBy(Member m)            { this.paidBy = m; }

    public Group getGroup()                    { return group; }
    public void setGroup(Group g)              { this.group = g; }

    public List<Member> getParticipants()      { return participants; }
    public void setParticipants(List<Member> p){ this.participants = p; }
}
