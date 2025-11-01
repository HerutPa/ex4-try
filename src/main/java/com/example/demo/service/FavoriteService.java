package com.example.demo.service;

import com.example.demo.model.Job;
import com.example.demo.model.User;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteService {

    private final UserRepository userRepo;
    private final JobRepository jobRepo;

    public FavoriteService(UserRepository userRepo, JobRepository jobRepo) {
        this.userRepo = userRepo;
        this.jobRepo = jobRepo;
    }

    /**
     * Toggle favorite: אם קיים - מסיר, אם לא קיים - מוסיף
     * @return true אם הוסיף, false אם הסיר
     */
    @Transactional
    public boolean toggleFavorite(Long userId, Long jobId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Job job = jobRepo.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (user.isFavorite(job)) {
            user.removeFavoriteJob(job);
            userRepo.save(user);
            return false; // removed
        } else {
            user.addFavoriteJob(job);
            userRepo.save(user);
            return true; // added
        }
    }

    /**
     * בדיקה האם משרה מסוימת היא מועדפת עבור משתמש
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long jobId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Job job = jobRepo.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        return user.isFavorite(job);
    }

    /**
     * קבלת כל המשרות המועדפות של משתמש (עם Pagination)
     */
    @Transactional(readOnly = true)
    public Page<Job> getFavoriteJobs(Long userId, Pageable pageable) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Job> favorites = new ArrayList<>(user.getFavoriteJobs());

        // Manual pagination מכיוון שזה Set
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), favorites.size());

        List<Job> pageContent = favorites.subList(start, end);

        return new PageImpl<>(pageContent, pageable, favorites.size());
    }

    /**
     * ספירת משרות מועדפות למשתמש
     */
    @Transactional(readOnly = true)
    public long countFavorites(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return user.getFavoriteJobs().size();
    }
}