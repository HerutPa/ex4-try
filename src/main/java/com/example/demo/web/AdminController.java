package com.example.demo.web;

import com.example.demo.repository.*;
import com.example.demo.service.JobService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final JobRepository jobRepo;
    private final UserRepository userRepo;
    private final JobCategoryRepository categoryRepo;
    private final ReviewRepository reviewRepo;
    private final JobService jobService;

    public AdminController(JobRepository jobRepo,
                           UserRepository userRepo,
                           JobCategoryRepository categoryRepo,
                           ReviewRepository reviewRepo,
                           JobService jobService) {
        this.jobRepo = jobRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.reviewRepo = reviewRepo;
        this.jobService = jobService;
    }

    // ===== ניהול משרות =====

    @GetMapping("/jobs")
    public String manageJobs(@PageableDefault(size = 20, sort = "createdAt",
                                     direction = Sort.Direction.DESC) Pageable pageable,
                             Model model) {
        model.addAttribute("page", jobRepo.findAll(pageable));
        return "admin/jobs";
    }

    @PostMapping("/jobs/{id}/delete")
    public String deleteJob(@PathVariable Long id, RedirectAttributes ra) {
        try {
            jobRepo.deleteById(id);
            ra.addFlashAttribute("msg", "✅ המשרה נמחקה בהצלחה!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "❌ שגיאה במחיקת המשרה: " + e.getMessage());
        }
        return "redirect:/admin/jobs";
    }

    // ===== ניהול משתמשים =====

    @GetMapping("/users")
    public String manageUsers(@PageableDefault(size = 20, sort = "createdAt",
                                      direction = Sort.Direction.DESC) Pageable pageable,
                              Model model) {
        model.addAttribute("page", userRepo.findAll(pageable));
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            // בדיקה שלא מוחקים את עצמו
            var user = userRepo.findById(id).orElseThrow();
            if (user.getRole().name().equals("ADMIN")) {
                ra.addFlashAttribute("error", "❌ לא ניתן למחוק משתמש ADMIN!");
                return "redirect:/admin/users";
            }

            userRepo.deleteById(id);
            ra.addFlashAttribute("msg", "✅ המשתמש נמחק בהצלחה!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "❌ שגיאה במחיקת המשתמש: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ===== ניהול קטגוריות =====

    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", categoryRepo.findAll(Sort.by("name")));
        return "admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryRepo.deleteById(id);
            ra.addFlashAttribute("msg", "✅ הקטגוריה נמחקה בהצלחה!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "❌ לא ניתן למחוק - קיימות משרות בקטגוריה זו");
        }
        return "redirect:/admin/categories";
    }

    // ===== ניהול ביקורות =====

    @GetMapping("/reviews")
    public String manageReviews(@PageableDefault(size = 20, sort = "createdAt",
                                        direction = Sort.Direction.DESC) Pageable pageable,
                                Model model) {
        model.addAttribute("page", reviewRepo.findAll(pageable));
        return "admin/reviews";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes ra) {
        try {
            reviewRepo.deleteById(id);
            ra.addFlashAttribute("msg", "✅ הביקורת נמחקה בהצלחה!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "❌ שגיאה במחיקת הביקורת: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
}