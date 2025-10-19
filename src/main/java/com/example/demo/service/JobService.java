package com.example.demo.service;

import com.example.demo.model.Job;
import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {

    Page<Job> getAllActiveJobs(Pageable pageable);

    Job getByIdOrThrow(Long id);

    Page<Job> getByPublisher(User publisher, Pageable pageable);

    Job getByIdAndPublisherOrThrow(Long id, Long publisherId);

    Job save(Job job);

    void deleteByIdAndPublisher(Long id, Long publisherId);

    // בדיקות ייחודיות ל-externalUrl
    boolean existsByExternalUrlIgnoreCase(String url);

    boolean existsByExternalUrlIgnoreCaseAndIdNot(String url, Long id);
}
