package com.example.demo.web;

import com.example.demo.model.Skill;
import com.example.demo.service.SkillService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService service;

    public SkillController(SkillService service) {
        this.service = service;
    }

    @GetMapping
    public List<Skill> getAll() {
        return service.getAllSkills();
    }

    @PostMapping
    public Skill add(@RequestParam String name) {
        return service.addSkill(name);
    }
}
