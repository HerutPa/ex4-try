package com.example.demo.web;

import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;
import com.example.demo.model.Role;

public class RegisterForm {

    @Email @NotBlank
    private String email;

    @NotBlank
    private String fullName;

    @NotNull
    private Role role; // enum USER/PUBLISHER/ADMIN

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    // חשוב: אוסף של String, עם getter+setter באותו טיפוס!
    private List<String> skills = new ArrayList<>();

    // טקסט חופשי
    private String freeTextSkills;

    // --- getters/setters (אותו טיפוס בדיוק) ---
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; } // ← אותו טיפוס

    public String getFreeTextSkills() { return freeTextSkills; }
    public void setFreeTextSkills(String freeTextSkills) { this.freeTextSkills = freeTextSkills; }
}
