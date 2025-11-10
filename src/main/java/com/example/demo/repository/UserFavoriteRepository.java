package com.example.demo.repository;

import com.example.demo.model.Job;
import com.example.demo.model.User;
import com.example.demo.model.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    // בדיקה אם משרה כבר במועדפים של המשתמש
    boolean existsByUserAndJob(User user, Job job);

    // מציאת רשומת מועדף ספציפית
    Optional<UserFavorite> findByUserAndJob(User user, Job job);

    // קבלת כל המשרות המועדפות של משתמש (עם Pagination)
    @Query("SELECT f.job FROM UserFavorite f WHERE f.user = :user ORDER BY f.createdAt DESC")
    Page<Job> findFavoriteJobsByUser(@Param("user") User user, Pageable pageable);

    // מחיקת מועדף
    void deleteByUserAndJob(User user, Job job);

    // ✅ NEW: מחיקת כל המועדפים של משרה מסוימת
    @Modifying
    void deleteByJob(Job job);

    // ספירת מועדפים של משתמש
    long countByUser(User user);
}