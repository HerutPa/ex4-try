package com.example.demo.web;

import com.example.demo.model.Job;
import com.example.demo.model.User;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;

@Controller
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepo;
    private final JobRepository jobRepo;

    public FavoriteController(FavoriteService favoriteService,
                              UserRepository userRepo,
                              JobRepository jobRepo) {
        this.favoriteService = favoriteService;
        this.userRepo = userRepo;
        this.jobRepo = jobRepo;
    }


    /**
     * דף המשרות המועדפות (HTML)
     */
    @GetMapping("/user/favorites")
    @PreAuthorize("hasRole('USER')")
    public String favorites(@PageableDefault(size = 10) Pageable pageable,
                            Principal principal,
                            Model model) {
        User user = findCurrentUser(principal);
        Page<Job> favorites = favoriteService.getFavoriteJobs(user, pageable);

        model.addAttribute("page", favorites);
        model.addAttribute("favoritesCount", favoriteService.countFavorites(user));

        return "user/favorites";
    }

    /**
     * Toggle מועדף (AJAX) - POST
     */
    @PostMapping("/jobs/{id}/favorite")
    @PreAuthorize("hasRole('USER')")
    @ResponseBody
    public Map<String, Object> toggleFavorite(@PathVariable Long id,
                                              Principal principal) {
        User user = findCurrentUser(principal);
        Job job = jobRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        boolean isFavorite = favoriteService.toggleFavorite(user, job);

        return Map.of(
                "success", true,
                "favorite", isFavorite,
                "message", isFavorite ? "נוסף למועדפים" : "הוסר מהמועדפים"
        );
    }

    /**
     * בדיקת סטטוס מועדף (AJAX) - GET
     */
    @GetMapping("/jobs/{id}/favorite-status")
    @PreAuthorize("hasRole('USER')")
    @ResponseBody
    public Map<String, Object> checkFavoriteStatus(@PathVariable Long id,
                                                   Principal principal) {
        User user = findCurrentUser(principal);
        Job job = jobRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        boolean isFavorite = favoriteService.isFavorite(user, job);

        return Map.of(
                "favorite", isFavorite,
                "jobId", id
        );
    }

    /**
     * מציאת המשתמש הנוכחי
     */
    private User findCurrentUser(Principal principal) {
        String email = principal.getName();
        return userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }
}