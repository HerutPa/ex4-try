package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public User register(String email, String rawPassword, String fullName) {
        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        String hash = encoder.encode(rawPassword);
        return users.save(new User(email, hash, fullName, Role.USER));
    }

    @Transactional
    public User register(String email, String rawPassword, String fullName, Role role) {
        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        // לא לאפשר הרשמה עצמית כ־ADMIN
        Role safeRole = (role == Role.PUBLISHER) ? Role.PUBLISHER : Role.USER;

        String hash = encoder.encode(rawPassword);
        return users.save(new User(email, hash, fullName, safeRole));
    }


    @Transactional(readOnly = true)
    public User getByEmailOrThrow(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found by email: " + email));
    }
}
