package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    private LocalDateTime createdAt = LocalDateTime.now();

    // קשר Many-to-Many עם טבלת JOB
    @ManyToMany(mappedBy = "skills")
    private Set<Job> jobs = new HashSet<>();

    // קשר Many-to-Many עם טבלת USER
    @ManyToMany(mappedBy = "skills")
    private Set<User> users = new HashSet<>();

    // ===== Constructors =====
    public Skill() {
    }

    public Skill(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Set<Job> getJobs() { return jobs; }
    public void setJobs(Set<Job> jobs) { this.jobs = jobs; }

    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }
}
