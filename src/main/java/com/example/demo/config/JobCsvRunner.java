package com.example.demo.config;

import com.example.demo.service.JobImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; // ← זה
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component; // ← וזה

@Component
@ConditionalOnProperty(value = "ex4.jobs.import-on-start", havingValue = "true")
public class JobCsvRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(JobCsvRunner.class);

    @Value("${ex4.jobs.csv-path:classpath:data/jobs.csv}")
    private Resource csv;

    private final JobImportService importer;
    public JobCsvRunner(JobImportService importer) { this.importer = importer; }

    @Override
    public void run(String... args) {
        try {
            if (!csv.exists()) {
                log.warn("Jobs CSV not found: {}", csv);
                return;
            }
            var res = importer.importCsv(csv);
            log.info("Imported jobs: total={}, inserted={}, updated={}, skipped={}",
                    res.total(), res.inserted(), res.skipped());
        } catch (Exception e) {
            log.error("Jobs CSV import error", e);
        }
    }
}
