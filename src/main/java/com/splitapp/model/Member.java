package com.splitapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    // ── Constructors ──────────────────────────────────────────────────────────
    public Member() {}
    public Member(String name, Group group) {
        this.name = name;
        this.group = group;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public Long getId()              { return id; }
    public void setId(Long id)       { this.id = id; }

    public String getName()          { return name; }
    public void setName(String name) { this.name = name; }

    public Group getGroup()          { return group; }
    public void setGroup(Group g)    { this.group = g; }
}
