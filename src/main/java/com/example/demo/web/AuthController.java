package com.example.demo.web;

import com.example.demo.repository.SkillRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class AuthController {

    private final UserService userService;
    private final SkillRepository skillRepo;
    private final UserRepository userRepo;

    public AuthController(UserService userService,
                          SkillRepository skillRepo,
                          UserRepository userRepo) {
        this.userService = userService;
        this.skillRepo = skillRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/login")
    public String login() { return "login"; }


    // AuthController (או RegisterController)
    // AuthController.java
    @GetMapping("/register")
    public String showRegister(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterForm());
        }
        model.addAttribute("allSkills", skillRepo.findAll(Sort.by("name")));
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("form") RegisterForm form,
                             BindingResult binding, RedirectAttributes ra) {

        if (form.getEmail()!=null) form.setEmail(form.getEmail().trim().toLowerCase());
        if (form.getFullName()!=null) form.setFullName(form.getFullName().trim());

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            binding.rejectValue("confirmPassword","mismatch","Passwords do not match");
        }
        if (userRepo.existsByEmailIgnoreCase(form.getEmail())) {
            binding.rejectValue("email","duplicate","Email already registered");
        }
        // ❌ להסיר:
        // if (userRepo.existsByFullNameIgnoreCase(form.getFullName())) { ... }

        if (binding.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", binding);
            ra.addFlashAttribute("form", form);
            return "redirect:/register";
        }

        if (form.getRole() == null) {
            binding.rejectValue("role","required","Role is required");
        }

        userService.register(
                form.getEmail(), form.getPassword(), form.getFullName(),
                form.getRole(), form.getSkills(), form.getFreeTextSkills()
        );

        ra.addFlashAttribute("msg","Registration successful. You can log in now.");
        return "redirect:/login?registered";
    }

}
