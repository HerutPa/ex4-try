package com.example.demo.service;

import com.example.demo.model.Job;
import com.example.demo.model.JobCategory;
import com.example.demo.model.Skill;             // ✅
import com.example.demo.repository.JobCategoryRepository;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.SkillRepository; // ✅
import com.opencsv.CSVReaderHeaderAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class JobImportService {
    private static final Logger log = LoggerFactory.getLogger(JobImportService.class);

    private final JobRepository jobs;
    private final JobCategoryRepository categories;
    private final SkillRepository skillsRepo; // ✅

    public JobImportService(JobRepository jobs,
                            JobCategoryRepository categories,
                            SkillRepository skillsRepo) { // ✅
        this.jobs = jobs;
        this.categories = categories;
        this.skillsRepo = skillsRepo; // ✅
    }

    /**
     * קורא CSV בעמודות:
     * id,company,title,description,location,salaryMin,salaryMax,externalUrl,isActive,createdAt,category,skills
     *  skills מופרד ב- ';' (למשל: "Java; Spring Boot; SQL")
     */
    @Transactional
    public ImportResult importCsv(Resource csv) {
        int inserted = 0, updated = 0, skipped = 0, total = 0;

        try (var reader = new CSVReaderHeaderAware(
                new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8))) {

            Map<String, String> row;
            boolean loggedHeaders = false;

            while ((row = reader.readMap()) != null) {
                total++;

                if (!loggedHeaders) {
                    log.info("CSV headers detected: {}", row.keySet()); // עוזר דיבוג
                    loggedHeaders = true;
                }

                String company      = get(row, "company");
                String title        = get(row, "title");
                String description  = get(row, "description");
                String location     = get(row, "location");
                String salaryMinStr = get(row, "salaryMin");
                String salaryMaxStr = get(row, "salaryMax");
                String externalUrl  = get(row, "externalUrl");
                String isActiveStr  = get(row, "isActive");
                String createdAtStr = get(row, "createdAt");
                String categoryName = get(row, "category");
                String skillsStr    = getCI(row, "skills"); // ✅ לוקח בלי תלות באותיות/רווחים

                // מינימום חובה
                if (isBlank(company) || isBlank(title) || isBlank(description)
                        || isBlank(externalUrl) || isBlank(categoryName)) {
                    skipped++;
                    continue;
                }

                JobCategory category = resolveCategory(categoryName);

                Job job = new Job();
                job.setCompany(company);
                job.setTitle(title);
                job.setDescription(description);
                job.setLocation(location);
                job.setSalaryMin(parseDouble(salaryMinStr));
                job.setSalaryMax(parseDouble(salaryMaxStr));
                job.setExternalUrl(externalUrl);
                job.setActive(parseBoolean(isActiveStr));
                Instant created = parseInstant(createdAtStr);
                if (created != null) job.setCreatedAt(created);
                job.setCategory(category);

                // ===== Skills מה-CSV =====
                if (!isBlank(skillsStr)) {
                    Set<Skill> skillSet = new HashSet<>();
                    for (String raw : skillsStr.split(";")) {   // מפריד ; כמו בדוגמאות שלך
                        String name = raw.trim().replaceAll("\\s+", " ");
                        if (name.isEmpty()) continue;

                        Skill s = skillsRepo.findByName(name)
                                .orElseGet(() -> skillsRepo.save(new Skill(name)));
                        skillSet.add(s);
                    }
                    job.setSkills(skillSet); // ייצור שורות ב-job_skills בעת השמירה
                } else {
                    log.debug("No skills for job '{}'", title);
                }

                jobs.save(job);
                inserted++;
            }
        } catch (Exception e) {
            log.error("CSV import failed", e);
            throw new RuntimeException("CSV import failed: " + e.getMessage(), e);
        }

        log.info("Jobs CSV import done: total={}, inserted={}, updated={}, skipped={}",
                total, inserted, updated, skipped);
        return new ImportResult(total, inserted, updated, skipped);
    }

    public record ImportResult(int total, int inserted, int updated, int skipped) {}

    private JobCategory resolveCategory(String name) {
        String n = (name == null || name.isBlank()) ? "Other" : name.trim();
        return categories.findByNameIgnoreCase(n)
                .orElseGet(() -> {
                    JobCategory c = new JobCategory();
                    c.setName(n);
                    return categories.save(c);
                });
    }

    private static String get(Map<String, String> row, String key) {
        String v = row.get(key);
        return v == null ? null : v.trim();
    }

    // ✅ שליפה חסינה רגישות-אותיות/רווחים לשם העמודה (OpenCSV רגיש מאוד לשמות)
    private static String getCI(Map<String, String> row, String key) {
        if (row == null || key == null) return null;
        for (var e : row.entrySet()) {
            if (e.getKey() == null) continue;
            if (e.getKey().trim().equalsIgnoreCase(key.trim())) {
                return e.getValue() == null ? null : e.getValue().trim();
            }
        }
        return null;
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    private static Double parseDouble(String v) {
        if (isBlank(v)) return null;
        try { return Double.parseDouble(v.trim()); }
        catch (Exception e) { return null; }
    }

    private static boolean parseBoolean(String v) {
        if (isBlank(v)) return false;
        v = v.trim().toLowerCase(Locale.ROOT);
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("y");
    }

    private static Instant parseInstant(String v) {
        if (isBlank(v)) return null;
        try {
            return Instant.parse(v);
        } catch (Exception ignore) {
            try {
                var ldt = LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return ldt.atZone(ZoneId.systemDefault()).toInstant();
            } catch (Exception e2) {
                return null;
            }
        }
    }
}
