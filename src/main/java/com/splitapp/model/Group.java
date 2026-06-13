package com.splitapp.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────────────────────
    public Group() {}
    public Group(String name) { this.name = name; }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }

    public String getName()                 { return name; }
    public void setName(String name)        { this.name = name; }

    public List<Member> getMembers()        { return members; }
    public void setMembers(List<Member> m)  { this.members = m; }

    public List<Expense> getExpenses()      { return expenses; }
    public void setExpenses(List<Expense> e){ this.expenses = e; }
}
