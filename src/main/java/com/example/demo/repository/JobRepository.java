package com.example.demo.repository;

import com.example.demo.model.Job;
import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByIsActiveTrue(Pageable pageable);

    Page<Job> findByPublisher(User publisher, Pageable pageable);

    Optional<Job> findByIdAndPublisherId(Long id, Long publisherId);

    @Query("select j from Job j join fetch j.category where j.id = :id and j.publisher.id = :publisherId")
    Optional<Job> findWithCategoryByIdAndPublisherId(@Param("id") Long id, @Param("publisherId") Long publisherId);

    // ---- חדש: בדיקות ייחודיות לפי externalUrl ----
    boolean existsByExternalUrlIgnoreCase(String externalUrl);

    boolean existsByExternalUrlIgnoreCaseAndIdNot(String externalUrl, Long id);
}
