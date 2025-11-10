package com.example.demo.service;

import com.example.demo.model.Job;
import com.example.demo.model.User;
import com.example.demo.model.UserFavorite;
import com.example.demo.repository.UserFavoriteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {

    private final UserFavoriteRepository favoriteRepo;

    public FavoriteService(UserFavoriteRepository favoriteRepo) {
        this.favoriteRepo = favoriteRepo;
    }

    /**
     * בדיקה אם משרה במועדפים
     */
    public boolean isFavorite(User user, Job job) {
        return favoriteRepo.existsByUserAndJob(user, job);
    }

    /**
     * הוספה/הסרה של משרה מהמועדפים (Toggle)
     */
    @Transactional
    public boolean toggleFavorite(User user, Job job) {
        if (favoriteRepo.existsByUserAndJob(user, job)) {
            // אם קיים - מוחקים
            favoriteRepo.deleteByUserAndJob(user, job);
            return false; // לא במועדפים יותר
        } else {
            // אם לא קיים - מוסיפים
            UserFavorite fav = new UserFavorite(user, job);
            favoriteRepo.save(fav);
            return true; // נוסף למועדפים
        }
    }

    /**
     * קבלת כל המשרות המועדפות של משתמש
     */
    public Page<Job> getFavoriteJobs(User user, Pageable pageable) {
        return favoriteRepo.findFavoriteJobsByUser(user, pageable);
    }

    /**
     * ספירת מועדפים
     */
    public long countFavorites(User user) {
        return favoriteRepo.countByUser(user);
    }
}