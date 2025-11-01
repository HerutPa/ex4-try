package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.Skill;
import com.example.demo.model.User;
import com.example.demo.repository.SkillRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final SkillRepository skillRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, SkillRepository skillRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.skillRepo = skillRepo;
        this.encoder = encoder;
    }

    /** שמירה ותאימות לאחור: רישום USER בלי skills */
    @Transactional
    public User register(String email, String rawPassword, String fullName) {
        return register(email, rawPassword, fullName, Role.USER, Collections.emptyList(), null);
    }

    /** שמירה ותאימות לאחור: רישום עם Role בלי skills */
    @Transactional
    public User register(String email, String rawPassword, String fullName, Role role) {
        return register(email, rawPassword, fullName, role, Collections.emptyList(), null);
    }

    /** רישום מלא: כולל צירוף skills (Names + free text) ושמירה ל-user_skills */
    @Transactional
    public User register(String email,
                         String rawPassword,
                         String fullName,
                         Role role,
                         List<String> selectedSkillNames,
                         String freeTextSkills) {

        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email is required");
        if (rawPassword == null || rawPassword.isBlank())
            throw new IllegalArgumentException("Password is required");
        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("Full name is required");

        if (userRepo.existsByEmailIgnoreCase(email))
            throw new IllegalArgumentException("Email already in use");

        // לא לאפשר הרשמה עצמית כ-ADMIN
        Role safeRole = (role == Role.PUBLISHER) ? Role.PUBLISHER : Role.USER;

        User u = new User();
        u.setEmail(email.trim());
        u.setFullName(fullName.trim());
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setRole(safeRole);

        // מצרפים skills רק למשתמשי USER
        if (safeRole == Role.USER) {
            Set<String> names = new HashSet<>();
            if (selectedSkillNames != null) {
                for (String s : selectedSkillNames) {
                    if (s != null && !s.isBlank()) names.add(s.trim());
                }
            }
            if (freeTextSkills != null) {
                for (String token : freeTextSkills.split("[,;]")) {
                    String s = token.trim();
                    if (!s.isEmpty()) names.add(s);
                }
            }

            if (!names.isEmpty()) {
                Set<Skill> entities = new HashSet<>();
                for (String name : names) {
                    Skill sk = skillRepo.findByNameIgnoreCase(name)
                            .orElseGet(() -> skillRepo.save(new Skill(name)));
                    entities.add(sk);
                }
                u.setSkills(entities); // 👈 זה יוצר שורות ב-user_skills
            }
        }

        return userRepo.save(u);
    }

    @Transactional(readOnly = true)
    public User getByEmailOrThrow(String email) {
        return userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found by email: " + email));
    }
}
