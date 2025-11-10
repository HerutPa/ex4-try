package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.HashSet;

import java.time.Instant;
// com.example.demo.model.User
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email",     columnNames = "email"),
        },
        indexes = {
                @Index(name = "idx_users_email",     columnList = "email"),
        }
)

public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(nullable = false, length = 200)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 60)   // BCrypt
    private String passwordHash;

    @NotBlank
    @Column(name="full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public User() {}
    public User(String email, String passwordHash, String fullName, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    @ManyToMany
    @JoinTable(
            name = "user_skills",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"})
    )
    private Set<Skill> skills = new HashSet<>();

    public void setSkills(Set<Skill> skills){ this.skills = skills; }

    public Set<Skill> getSkills() { return skills; }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();

    public Set<Review> getReviews() { return reviews; }
    public void setReviews(Set<Review> reviews) { this.reviews = reviews; }

    // ===== 🆕 קשר למשרות מועדפות =====
    @ManyToMany
    @JoinTable(
            name = "user_favorite_jobs",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "job_id"),
            indexes = {
                    @Index(name = "idx_user_favorites_user", columnList = "user_id"),
                    @Index(name = "idx_user_favorites_job", columnList = "job_id")
            }
    )
    private Set<Job> favoriteJobs = new HashSet<>();

    public Set<Job> getFavoriteJobs() { return favoriteJobs; }
    public void setFavoriteJobs(Set<Job> favoriteJobs) { this.favoriteJobs = favoriteJobs; }

    // helper methods for favorites
    public void addFavoriteJob(Job job) {
        this.favoriteJobs.add(job);
    }

    public void removeFavoriteJob(Job job) {
        this.favoriteJobs.remove(job);
    }

    public boolean isFavorite(Job job) {
        return this.favoriteJobs.contains(job);
    }

    @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }

    // getters & setters
    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Instant getCreatedAt() { return createdAt; }

}