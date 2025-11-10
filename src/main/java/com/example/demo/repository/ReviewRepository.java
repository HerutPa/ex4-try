package com.example.demo.repository;

import com.example.demo.model.Review;
import com.example.demo.model.Review.Status;
import com.example.demo.model.Job;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ReviewRepository extends JpaRepository<Review, Long> {
    // לפי Job ספציפי (אם תרצה מסך פר-משרה)
    List<Review> findByJobAndStatusOrderByCreatedAtDesc(Job job, Status status);

    // אם תרצה להימנע מ-N+1 ב-Thymeleaf (טוען גם job וגם user)
    @EntityGraph(attributePaths = {"job", "user"})
    List<Review> findByJobOrderByCreatedAtDesc(Job job);
}
