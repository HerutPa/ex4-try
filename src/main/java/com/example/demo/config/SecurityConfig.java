package com.example.demo.config;

import com.example.demo.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // חייב להתאים להאש שמור ב-DB
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        // התחברות לפי אימייל (username = email)
        return email -> repo.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .map(u -> {
                    String roleName = u.getRole().name(); // enum: USER / PUBLISHER / ADMIN
                    return org.springframework.security.core.userdetails.User
                            .withUsername(u.getEmail())
                            .password(u.getPasswordHash())
                            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + roleName)))
                            .accountExpired(false).accountLocked(false)
                            .credentialsExpired(false).disabled(false)
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public DaoAuthenticationProvider authProvider(UserDetailsService uds, PasswordEncoder enc) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(enc);
        return p;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider authProvider) throws Exception {
        return http
                // נוודא שה־AuthenticationProvider שלנו בשימוש
                .authenticationProvider(authProvider)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/login",
                                "/register", "/register/**",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/register").permitAll() // חשוב
                        .requestMatchers("/jobs/**").hasAnyRole("USER","ADMIN")
                        .requestMatchers("/publisher/**").hasAnyRole("PUBLISHER","ADMIN")
                        .requestMatchers("/post-login").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER","PUBLISHER","ADMIN")
                        .anyRequest().authenticated()
                )


                .formLogin(f -> f
                        .loginPage("/login").permitAll()
                        .loginProcessingUrl("/login")   // ה-POST של הטופס
                        .usernameParameter("email")     // שמות השדות בטופס
                        .passwordParameter("password")
                        // במקום defaultSuccessUrl — מפנים לנתב תפקידים שלנו
                        .successHandler((req, res, authn) -> res.sendRedirect("/post-login"))
                        .failureUrl("/login?error")
                )

                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")          // אחרי Logout חוזרים ל־/
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // למנוע הצגה מקאש לאחר Logout (Back בדפדפן)
                .headers(h -> h.cacheControl(c -> { }))

                // אם יש לך H2-Console אפשר להוסיף כאן החרגה של CSRF ו-FrameOptions
                .build();
    }
}
