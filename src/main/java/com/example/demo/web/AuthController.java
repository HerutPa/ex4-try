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


    // AuthController (או RegisterController)
    @GetMapping("/register")
    public String showRegister(Model model) {
        if (!model.containsAttribute("form")) model.addAttribute("form", new RegisterForm());
        model.addAttribute("allSkills", skillRepo.findAll(Sort.by("name"))); // חשוב לטמפלט
        return "register";
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("form") RegisterForm form,
                             BindingResult binding,
                             RedirectAttributes ra,
                             Model model) {
        // תוספת: נרמל קלטים (למניעת רווחים מיותרים)
        if (form.getEmail() != null)    form.setEmail(form.getEmail().trim());
        if (form.getFullName() != null) form.setFullName(form.getFullName().trim());

        // בדיקת התאמת סיסמאות
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            binding.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (binding.hasErrors()) {
            model.addAttribute("form", form);
            model.addAttribute("allSkills", skillRepo.findAll(Sort.by("name")));
            return "register";
        }

        try {
            userService.register(
                    form.getEmail(),
                    form.getPassword(),
                    form.getFullName(),
                    form.getRole(),
                    form.getSkills(),         // ← שמות ה-skills מהצ׳קבוקסים
                    form.getFreeTextSkills()  // ← טקסט חופשי (פסיקים/נקודה-פסיק)
            );
        } catch (IllegalArgumentException ex) {
            binding.rejectValue("email", "exists", ex.getMessage());
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("allSkills", skillRepo.findAll(Sort.by("name")));
            return "register";
        }


        // Flash + סמן ב-URL כדי להראות הודעת הצלחה בדף הלוגין
        ra.addFlashAttribute("msg", "Registration successful. You can log in now.");
        return "redirect:/login?registered";
    }
}
