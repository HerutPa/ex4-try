package com.example.demo.service;

import com.example.demo.model.Job;
import com.example.demo.model.Review;
import com.example.demo.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository repo;

    public ReviewService(ReviewRepository repo) {
        this.repo = repo;
    }

    public List<Review> getByJob(Job job) {
        return repo.findByJob(job);
    }

    public void save(Review review) {
        repo.save(review);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
