package com.example.demo.web;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;
import java.util.ArrayList;
import java.util.List;

public class JobForm {
    @NotBlank @Size(max=150)
    private String company;

    @NotBlank @Size(max=200)
    private String title;

    @NotBlank
    private String description;

    @Size(max=200)
    private String location;

    private Double salaryMin;
    private Double salaryMax;

    @NotBlank @URL @Size(max=1000)
    private String externalUrl;

    private Long categoryId;
    @Size(max=120)
    private String newCategoryName;

    private boolean active = true;

    private List<Long> skillIds = new ArrayList<>();  // לבחירה מרשימה קיימת (multi-select)
    @Size(max = 1000)
    private String skillsInput;                       // לקליטה חופשית בפסיקים/;

    @AssertTrue(message = "Select an existing category or enter a new category")
    public boolean isCategoryProvided() {
        return (categoryId != null) || (newCategoryName != null && !newCategoryName.trim().isEmpty());
    }

    // ===== Getters & Setters =====
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Double salaryMin) { this.salaryMin = salaryMin; }

    public Double getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Double salaryMax) { this.salaryMax = salaryMax; }

    public String getExternalUrl() { return externalUrl; }
    public void setExternalUrl(String externalUrl) { this.externalUrl = externalUrl; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getNewCategoryName() { return newCategoryName; }
    public void setNewCategoryName(String newCategoryName) { this.newCategoryName = newCategoryName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Long> getSkillIds() { return skillIds; }
    public void setSkillIds(List<Long> skillIds) { this.skillIds = skillIds; }

    public String getSkillsInput() { return skillsInput; }
    public void setSkillsInput(String skillsInput) { this.skillsInput = skillsInput; }
}