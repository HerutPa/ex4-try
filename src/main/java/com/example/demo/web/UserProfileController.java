package com.example.demo.web;

import com.example.demo.model.User;
import com.example.demo.repository.SkillRepository;
import com.example.demo.service.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user/profile")
@PreAuthorize("hasRole('USER')")
public class UserProfileController {

    private final UserService userService;
    private final SkillRepository skillRepo;

    public UserProfileController(UserService userService, SkillRepository skillRepo) {
        this.userService = userService;
        this.skillRepo = skillRepo;
    }

    /**
     * GET - דף עריכת כישורים
     */
    @GetMapping("/edit")
    public String showEditSkills(Principal principal, Model model) {
        User currentUser = userService.getByEmailOrThrow(principal.getName());

        if (!model.containsAttribute("form")) {
            // מילוי הטופס עם Skills קיימים
            SkillsEditForm form = new SkillsEditForm();

            // ממיר את ה-Skills של המשתמש לרשימת שמות
            if (currentUser.getSkills() != null && !currentUser.getSkills().isEmpty()) {
                form.setSkills(
                        currentUser.getSkills().stream()
                                .map(s -> s.getName())
                                .collect(Collectors.toList())
                );
            }

            model.addAttribute("form", form);
        }

        // כל ה-Skills הזמינים במערכת
        model.addAttribute("allSkills", skillRepo.findAll(Sort.by("name")));
        model.addAttribute("currentUser", currentUser);

        return "user/edit-skills";
    }

    /**
     * POST - שמירת עדכון כישורים
     */
    @PostMapping("/edit")
    public String updateSkills(@ModelAttribute("form") SkillsEditForm form,
                               Principal principal,
                               RedirectAttributes ra) {
        try {
            User currentUser = userService.getByEmailOrThrow(principal.getName());

            // עדכון ה-Skills
            userService.updateUserSkills(
                    currentUser.getId(),
                    form.getSkills(),
                    form.getFreeTextSkills()
            );

            ra.addFlashAttribute("msg", "✅ הכישורים שלך עודכנו בהצלחה!");
            return "redirect:/user/home";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "❌ שגיאה בעדכון הכישורים: " + e.getMessage());
            ra.addFlashAttribute("form", form);
            return "redirect:/user/profile/edit";
        }
    }
}