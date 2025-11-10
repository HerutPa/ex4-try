package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email); // ← הוסיפי שורה זו
    boolean existsByFullNameIgnoreCase(String fullName); // רק אם תרצה להתריע (לא לחסום)
}
