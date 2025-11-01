package com.example.demo.web;

import com.example.demo.repository.UserRepository;
import com.example.demo.service.FavoriteService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@PreAuthorize("hasRole('USER')")
public class UserFavoritesController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepo;

    public UserFavoritesController(FavoriteService favoriteService, UserRepository userRepo) {
        this.favoriteService = favoriteService;
        this.userRepo = userRepo;
    }

    /**
     * דף המשרות המועדפות של המשתמש
     */
    @GetMapping("/user/favorites")
    public String myFavorites(@PageableDefault(size = 10) Pageable pageable,
                              Principal principal,
                              Model model) {
        var user = getCurrentUser(principal);
        var favoritesPage = favoriteService.getFavoriteJobs(user.getId(), pageable);

        model.addAttribute("page", favoritesPage);
        model.addAttribute("totalFavorites", favoriteService.countFavorites(user.getId()));

        return "user/favorites";
    }

    /**
     * Toggle favorite (AJAX endpoint)
     * מחזיר JSON: {"favorite": true/false}
     */
    @PostMapping("/jobs/{jobId}/favorite")
    @ResponseBody
    public java.util.Map<String, Boolean> toggleFavorite(@PathVariable Long jobId,
                                                         Principal principal) {
        var user = getCurrentUser(principal);
        boolean isNowFavorite = favoriteService.toggleFavorite(user.getId(), jobId);

        return java.util.Map.of("favorite", isNowFavorite);
    }

    /**
     * בדיקת סטטוס מועדף (AJAX endpoint)
     */
    @GetMapping("/jobs/{jobId}/favorite-status")
    @ResponseBody
    public java.util.Map<String, Boolean> checkFavoriteStatus(@PathVariable Long jobId,
                                                              Principal principal) {
        var user = getCurrentUser(principal);
        boolean isFavorite = favoriteService.isFavorite(user.getId(), jobId);

        return java.util.Map.of("favorite", isFavorite);
    }

    private com.example.demo.model.User getCurrentUser(Principal principal) {
        String email = principal.getName();
        return userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }
}