// HomeController.java (מתוקן – בלי /login ו/או /register)
package com.example.demo.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

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

    @GetMapping("/user/home")
    public String userHome() { return "user/home"; }

    @GetMapping("/publisher/home")
    public String publisherHome() { return "publisher/home"; }

    @GetMapping("/admin/home")
    public String adminHome() { return "admin/home"; }

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
