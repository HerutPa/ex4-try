package com.example.demo.service.impl;

import com.example.demo.model.Job;
import com.example.demo.model.User;
import com.example.demo.repository.JobRepository;
import com.example.demo.service.JobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobs;

    public JobServiceImpl(JobRepository jobs) {
        this.jobs = jobs;
    }

    @Override
    public Page<Job> getAllActiveJobs(Pageable pageable) {
        return jobs.findByIsActiveTrue(pageable);
    }

    @Override
    public Job getByIdOrThrow(Long id) {
        return jobs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found: " + id));
    }

    @Override
    public Page<Job> getByPublisher(User publisher, Pageable pageable) {
        return jobs.findByPublisher(publisher, pageable);
    }

    @Override
    public Job getByIdAndPublisherOrThrow(Long id, Long publisherId) {
        return jobs.findByIdAndPublisherId(id, publisherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your job"));
    }

    @Override
    @Transactional
    public Job save(Job job) {
        return jobs.save(job);
    }

    @Override
    @Transactional
    public void deleteByIdAndPublisher(Long id, Long publisherId) {
        Job job = jobs.findByIdAndPublisherId(id, publisherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your job"));
        jobs.delete(job);
    }

    @Override
    public boolean existsByExternalUrlIgnoreCase(String url) {
        return jobs.existsByExternalUrlIgnoreCase(url);
    }

    @Override
    public boolean existsByExternalUrlIgnoreCaseAndIdNot(String url, Long id) {
        return jobs.existsByExternalUrlIgnoreCaseAndIdNot(url, id);
    }
}
