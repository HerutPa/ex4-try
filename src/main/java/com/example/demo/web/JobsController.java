package com.example.demo.web;

import com.example.demo.model.Review;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.service.JobImportService;
import com.example.demo.service.JobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/jobs")
public class JobsController {

    private final JobService jobService;
    private final JobImportService jobImportService;
    private final ReviewRepository reviewRepo;           // ✅ הוספנו
    private final Resource csvResource;

    public JobsController(
            JobService jobService,
            JobImportService jobImportService,
            ReviewRepository reviewRepo,                  // ✅ הוספנו
            @Value("${ex4.jobs.csv-path:classpath:data/jobs.csv}") Resource csvResource
    ) {
        this.jobService = jobService;
        this.jobImportService = jobImportService;
        this.reviewRepo = reviewRepo;                    // ✅ הוספנו
        this.csvResource = csvResource;
    }

    /** רשימת משרות פעילות לכולם */
    @GetMapping
    public String list(@PageableDefault(size = 10) Pageable pageable, Model model) {
        model.addAttribute("page", jobService.getAllActiveJobs(pageable));
        return "jobs/list";
    }

    // GET: פרטי משרה + טופס ביקורת
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var job = jobService.getByIdOrThrow(id);
        var reviews = reviewRepo.findByJobOrderByCreatedAtDesc(job); // ← בלי סטטוס
        double avg = reviews.stream()
                .mapToInt(r -> r.getRating() == null ? 0 : r.getRating())
                .average().orElse(0.0);

        model.addAttribute("job", job);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avg);
        model.addAttribute("reviewsCount", reviews.size());

        if (!model.containsAttribute("reviewForm")) {
            model.addAttribute("reviewForm", new ReviewForm());
        }

        return "jobs/detail";
    }


    // POST: שליחת ביקורת (רק למשתמשים מחוברים — שנה/הסר לפי הצורך)
    @PostMapping("/{id}/reviews")
    @PreAuthorize("isAuthenticated()")
    public String addReview(@PathVariable Long id,
                            @Valid @ModelAttribute("reviewForm") ReviewForm form,
                            BindingResult br,
                            RedirectAttributes ra) {
        var job = jobService.getByIdOrThrow(id);

        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.reviewForm", br);
            ra.addFlashAttribute("reviewForm", form);
            return "redirect:/jobs/{id}";
        }

        var r = new Review();
        r.setJob(job);
        r.setRating(form.getRating());
        r.setReviewerName(form.getReviewerName());
        r.setComment(form.getComment());
        reviewRepo.save(r);

        ra.addFlashAttribute("msg", "תודה! הביקורת התקבלה וממתינה לאישור.");
        return "redirect:/jobs/{id}";
    }
}
