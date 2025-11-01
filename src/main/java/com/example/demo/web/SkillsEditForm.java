package com.example.demo.web;

import java.util.ArrayList;
import java.util.List;

public class SkillsEditForm {

    private List<String> skills = new ArrayList<>();

    private String freeTextSkills;

    // Getters & Setters
    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getFreeTextSkills() {
        return freeTextSkills;
    }

    public void setFreeTextSkills(String freeTextSkills) {
        this.freeTextSkills = freeTextSkills;
    }
}