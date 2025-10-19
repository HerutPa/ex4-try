package com.example.demo.repository;

import com.example.demo.model.Review;
import com.example.demo.model.Review.Status;
import com.example.demo.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByJobAndStatusOrderByCreatedAtDesc(Job job, Status status);
    List<Review> findByJob(Job job);
    List<Review> findByJobOrderByCreatedAtDesc(Job job); // ← פשוט, בלי סטטוס
    long countByJobAndStatus(Job job, Status status);
}
