package com.example.demo.service;

import com.example.demo.model.Skill;
import com.example.demo.repository.SkillRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SkillService {

    private final SkillRepository repo;

    public SkillService(SkillRepository repo) {
        this.repo = repo;
    }

    public List<Skill> getAllSkills() {
        return repo.findAll();
    }

    public Skill addSkill(String name) {
        Skill skill = new Skill();
        skill.setName(name);
        return repo.save(skill);
    }
}
