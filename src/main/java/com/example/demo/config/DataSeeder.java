package com.example.demo.config;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev") // תרוץ רק כשspring.profiles.active=dev
public class DataSeeder {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder enc) {
        return args -> {
            final String adminEmail = "admin@example.com";
            final String fullName   = "Admin";
            final String raw        = "Admin123!";

            users.findByEmailIgnoreCase(adminEmail).orElseGet(() -> {
                log.warn("[DEV ONLY] Admin credentials -> email: {}  password: {}", adminEmail, raw);
                return users.save(new User(adminEmail, enc.encode(raw), fullName, Role.ADMIN));
            });
        };
    }
}
