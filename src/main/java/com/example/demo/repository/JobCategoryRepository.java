// com/example/demo/repository/JobCategoryRepository.java
package com.example.demo.repository;

import com.example.demo.model.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {
    Optional<JobCategory> findByNameIgnoreCase(String name);
}
