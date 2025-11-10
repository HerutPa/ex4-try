// HomeController.java
package com.example.demo.web;

import com.example.demo.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    private final UserRepository userRepo;

    public HomeController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/")
    public String root(Authentication auth) {
        if (isAuthenticated(auth)) {
            return "redirect:" + homeFor(auth);
        }
        return "index";
    }

    @GetMapping("/post-login")
    public String postLogin(Authentication auth) {
        if (!isAuthenticated(auth)) return "redirect:/login";
        return "redirect:" + homeFor(auth);
    }

    // === USER HOME ===
    @GetMapping("/user/home")
    public String userHome(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName(); // מגיע מספרינג סיקיוריטי
            userRepo.findByEmailIgnoreCase(email)
                    .ifPresent(u -> model.addAttribute("currentUser", u));
        }
        return "user/home";
    }

    // (לא חובה לשנות, משאירים כמו שהיה)
    @GetMapping("/publisher/home")
    public String publisherHome() { return "publisher/home"; }

    @GetMapping("/admin/home")
    public String adminHome() { return "admin/home"; }

    // === Helpers ===
    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private String homeFor(Authentication auth) {
        var roles = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        if (roles.contains("ROLE_ADMIN"))     return "/admin/home";
        if (roles.contains("ROLE_PUBLISHER")) return "/publisher/home";
        return "/user/home";
    }
}
