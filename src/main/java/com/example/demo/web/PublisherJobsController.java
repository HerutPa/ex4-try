package com.example.demo.web;

import com.example.demo.model.Job;
import com.example.demo.model.JobCategory;
import com.example.demo.model.Skill;
import com.example.demo.model.User;
import com.example.demo.repository.JobCategoryRepository;
import com.example.demo.repository.SkillRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JobService;
import com.example.demo.model.Review;
import com.example.demo.repository.ReviewRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/publisher/jobs")
@PreAuthorize("hasAnyRole('PUBLISHER','ADMIN')")
public class PublisherJobsController {

    private final JobService jobService;
    private final JobCategoryRepository categoryRepo;
    private final UserRepository userRepo;
    private final SkillRepository skillRepo; // ✅ חדש
    private final ReviewRepository reviewRepo; // ✅ חדש


    /** עמוד "העבודות שלי" של המפרסם */
    @GetMapping
    public String myJobs(Pageable pageable, Principal principal, Model model) {
        User publisher = findCurrentUser(principal);
        model.addAttribute("page", jobService.getByPublisher(publisher, pageable));
        if (!model.containsAttribute("form")) model.addAttribute("form", new JobForm());
        model.addAttribute("categories", categoryRepo.findAll(Sort.by("name")));
        model.addAttribute("allSkills", skillRepo.findAll(Sort.by("name"))); // ✅ לרנדר ה־multi-select
        return "publisher/jobs/my-jobs";
    }

    @GetMapping("/new")
    public String showCreate(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new JobForm());
        }
        model.addAttribute("categories", categoryRepo.findAll(Sort.by("name")));
        model.addAttribute("allSkills", skillRepo.findAll(Sort.by("name"))); // ✅
        return "publisher/jobs/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") JobForm form,
                         BindingResult br,
                         Principal principal,
                         RedirectAttributes ra,
                         Model model,
                         Pageable pageable) {

        if (br.hasErrors()) {
            User publisher = findCurrentUser(principal);
            model.addAttribute("page", jobService.getByPublisher(publisher, pageable));
            model.addAttribute("categories", categoryRepo.findAll(Sort.by("name")));
            model.addAttribute("allSkills", skillRepo.findAll(Sort.by("name"))); // ✅ חובה כשיש שגיאות
            return "publisher/jobs/my-jobs";
        }

        JobCategory category = resolveOrCreateCategory(form);
        User publisher = findCurrentUser(principal);

        Job job = new Job();
        job.setCompany(form.getCompany());
        job.setTitle(form.getTitle());
        job.setDescription(form.getDescription());
        job.setLocation(form.getLocation());
        job.setSalaryMin(form.getSalaryMin());
        job.setSalaryMax(form.getSalaryMax());
        job.setExternalUrl(form.getExternalUrl());
        job.setActive(form.isActive());
        job.setCategory(category);
        job.setPublisher(publisher);

        // ✅ שיוך מיומנויות
        job.setSkills(resolveSkillsFromForm(form));

        try {
            jobService.save(job);
            ra.addFlashAttribute("msg", "Job created successfully.");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            ra.addFlashAttribute("msg", "Job with this URL already exists.");
        }

        return "redirect:/publisher/jobs";
    }

    /** תגובות למשרה ספציפית של המפרסם (עם בדיקת בעלות) */
    @GetMapping("/{id}/reviews")
    public String jobReviews(@PathVariable Long id, Principal principal, Model model) {
        User publisher = findCurrentUser(principal);
        Job job = jobService.getByIdAndPublisherOrThrow(id, publisher.getId()); // בדיקת בעלות
        var reviews = reviewRepo.findByJobAndStatusOrderByCreatedAtDesc(job, Review.Status.APPROVED);
        model.addAttribute("job", job);
        model.addAttribute("reviews", reviews);
        return "publisher/reviews/job-reviews"; // צור טמפלייט זה
    }


    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") JobForm form,
                         BindingResult br,
                         Principal principal,
                         RedirectAttributes ra,
                         Model model) {
        User publisher = findCurrentUser(principal);
        Job job = jobService.getByIdAndPublisherOrThrow(id, publisher.getId());

        // בדיקת כפילות URL אצל רשומה אחרת
        if (!br.hasFieldErrors("externalUrl")
                && jobService.existsByExternalUrlIgnoreCaseAndIdNot(form.getExternalUrl(), id)) {
            br.rejectValue("externalUrl", "duplicate", "Another job with this URL already exists");
        }

        if (br.hasErrors()) {
            model.addAttribute("categories", categoryRepo.findAll(Sort.by("name")));
            model.addAttribute("allSkills", skillRepo.findAll(Sort.by("name"))); // ✅ להצגה מחודשת של הרשימה
            model.addAttribute("jobId", id);
            return "publisher/jobs/edit";
        }

        JobCategory category = resolveOrCreateCategory(form);

        job.setCompany(form.getCompany());
        job.setTitle(form.getTitle());
        job.setDescription(form.getDescription());
        job.setLocation(form.getLocation());
        job.setSalaryMin(form.getSalaryMin());
        job.setSalaryMax(form.getSalaryMax());
        job.setExternalUrl(form.getExternalUrl());
        job.setActive(form.isActive());
        job.setCategory(category);

        // ✅ עדכון מיומנויות
        job.setSkills(resolveSkillsFromForm(form));

        jobService.save(job);
        ra.addFlashAttribute("msg", "Job updated.");
        return "redirect:/publisher/jobs";
    }

    public PublisherJobsController(JobService jobService,
                                   JobCategoryRepository categoryRepo,
                                   UserRepository userRepo,
                                   SkillRepository skillRepo,
                                   ReviewRepository reviewRepo) { // ✅ הוסף פרמטר
        this.jobService = jobService;
        this.categoryRepo = categoryRepo;
        this.userRepo = userRepo;
        this.skillRepo = skillRepo;
        this.reviewRepo = reviewRepo; // ✅ השמה
    }

    // טופס עריכה
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Principal principal, Model model) {
        User publisher = findCurrentUser(principal);
        Job job = jobService.getByIdAndPublisherOrThrow(id, publisher.getId()); // בדיקת בעלות

        if (!model.containsAttribute("form")) {
            JobForm form = new JobForm();
            form.setCompany(job.getCompany());
            form.setTitle(job.getTitle());
            form.setDescription(job.getDescription());
            form.setLocation(job.getLocation());
            form.setSalaryMin(job.getSalaryMin());
            form.setSalaryMax(job.getSalaryMax());
            form.setExternalUrl(job.getExternalUrl());
            form.setCategoryId(job.getCategory() != null ? job.getCategory().getId() : null);
            form.setActive(job.isActive());
            // ✅ למלא בחירה קיימת של skills לעריכה (IDs)
            if (job.getSkills() != null && !job.getSkills().isEmpty()) {
                form.setSkillIds(job.getSkills().stream().map(Skill::getId).toList());
            }
            model.addAttribute("form", form);
        }
        model.addAttribute("categories", categoryRepo.findAll(Sort.by("name")));
        model.addAttribute("allSkills", skillRepo.findAll(Sort.by("name"))); // ✅
        model.addAttribute("jobId", id);
        return "publisher/jobs/edit";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        User publisher = findCurrentUser(principal);
        Job job = jobService.getByIdAndPublisherOrThrow(id, publisher.getId()); // בדיקת בעלות
        job.setActive(!job.isActive());
        jobService.save(job);
        ra.addFlashAttribute("msg", "Status changed.");
        return "redirect:/publisher/jobs";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        User publisher = findCurrentUser(principal);
        jobService.deleteByIdAndPublisher(id, publisher.getId()); // בודק בעלות ומוחק
        ra.addFlashAttribute("msg", "Job deleted.");
        return "redirect:/publisher/jobs";
    }

    private JobCategory resolveOrCreateCategory(JobForm form) {
        if (form.getNewCategoryName() != null && !form.getNewCategoryName().trim().isEmpty()) {
            String name = form.getNewCategoryName().trim();
            return categoryRepo.findByNameIgnoreCase(name)
                    .orElseGet(() -> {
                        JobCategory c = new JobCategory();
                        c.setName(name);
                        return categoryRepo.save(c);
                    });
        } else {
            return categoryRepo.findById(form.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category"));
        }
    }

    /** ✅ מאחד בחירה לפי IDs או קלט חופשי למערך Skills לשיוך ל-Job */
    private Set<Skill> resolveSkillsFromForm(JobForm form) {
        Set<Skill> out = new HashSet<>();

        List<Long> ids = form.getSkillIds();
        if (ids != null && !ids.isEmpty()) {
            out.addAll(skillRepo.findAllById(ids));
        }

        String raw = form.getSkillsInput();
        if (raw != null && !raw.isBlank()) {
            String[] parts = raw.split("[;,]");
            for (String p : parts) {
                String name = p.trim().replaceAll("\\s+", " ");
                if (name.isEmpty()) continue;
                Skill s = skillRepo.findByNameIgnoreCase(name)
                        .orElseGet(() -> skillRepo.save(new Skill(name)));
                out.add(s);
            }
        }
        return out;
    }

    /* ===== helpers ===== */
    private User findCurrentUser(Principal principal) {
        String email = principal.getName();
        return userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Current user not found: " + email));
    }
}
