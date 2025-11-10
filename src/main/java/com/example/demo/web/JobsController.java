package com.example.demo.web;

import com.example.demo.model.Review;
import com.example.demo.model.User;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
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
import java.util.List;                       // ← חובה! אחרת List לא מזוהה
import com.example.demo.model.Job;
import java.security.Principal;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.JobCategoryRepository;
import com.example.demo.model.Job;


@Controller
@RequestMapping("/jobs")
public class JobsController {

    private final JobService jobService;
    private final JobImportService jobImportService;
    private final ReviewRepository reviewRepo;
    private final Resource csvResource;
    private final UserRepository userRepo;
    private final JobRepository jobRepository; // ← הוספנו

    public JobsController(
            JobService jobService,
            JobImportService jobImportService,
            ReviewRepository reviewRepo,
            UserRepository userRepo,
            JobRepository jobRepository, // ← הוספנו
            @Value("${ex4.jobs.csv-path:classpath:data/jobs.csv}") Resource csvResource
    ) {
        this.jobImportService = jobImportService;
        this.reviewRepo = reviewRepo;
        this.csvResource = csvResource;
        this.jobService = jobService;
        this.userRepo = userRepo;
        this.jobRepository = jobRepository; // ← הוספנו
    }

    // GET: פרטי משרה + טופס ביקורת
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Job job = jobService.getByIdOrThrow(id);

        // אפשרות א: כל התגובות (בלי סטטוס)
        List<Review> reviews = reviewRepo.findByJobOrderByCreatedAtDesc(job);

        // אפשרות ב (במקום א): רק תגובות מאושרות
        // List<Review> reviews = reviewRepo.findByJobAndStatusOrderByCreatedAtDesc(job, Review.Status.APPROVED);

        double avg = reviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

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

    // ===== רשימת משרות + סינון (באותו endpoint) =====
    @GetMapping
    public String list(@PageableDefault(size = 10) Pageable pageable,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       Model model,
                       Principal principal) {

        // משתמש מחובר (לא חובה לפעולה, אבל משאירים כמו שהיה)
        if (principal != null) {
            String email = principal.getName();
            User user = userRepo.findByEmailIgnoreCase(email).orElse(null);
            model.addAttribute("currentUser", user);
        }

        // --- שליפת נתונים לפי פילטרים ---
        List<Job> all;  // הרשימה המלאה לפני חיתוך ע"י pageable

        boolean hasKeyword = (keyword != null && !keyword.isBlank());
        boolean hasCategory = (category != null && !category.isBlank());

        if (hasKeyword && hasCategory) {
            all = jobRepository.findByTitleContainingIgnoreCaseAndCategory_NameIgnoreCase(keyword.trim(), category.trim());
        } else if (hasCategory) {
            all = jobRepository.findByCategory_NameIgnoreCase(category.trim());
        } else if (hasKeyword) {
            all = jobRepository.findByTitleContainingIgnoreCase(keyword.trim());
        } else {
            // ברירת מחדל: משרות פעילות (אם תרצה – המשך להשתמש בשירות שלך)
            all = jobRepository.findAll();
            // אם יש לך שירות שמחזיר רק פעילות:
            // var pageActive = jobService.getAllActiveJobs(pageable);
            // ונשאר לעבוד איתו. כאן השארתי אחיד כדי שתהיה עקביות עם הפילטרים.
        }

        // --- בניית Page ידנית מהרשימה (כדי לעבוד עם אותה תבנית jobs/list) ---
        int pageSize = pageable.getPageSize();
        int current = pageable.getPageNumber();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageSize, all.size());
        List<Job> pageContent = start > end ? List.of() : all.subList(start, end);

        org.springframework.data.domain.Page<Job> page =
                new org.springframework.data.domain.PageImpl<>(pageContent, pageable, all.size());

        // --- קטגוריות ל־<select> ---
        List<String> categories = jobRepository.findDistinctCategories();

        // --- הזרקה למודל ---
        model.addAttribute("page", page);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", hasCategory ? category : null);
        model.addAttribute("keyword", hasKeyword ? keyword : null);

        return "jobs/list";
    }

    // אופציונלי: לשמור תאימות אם הטופס/קישורים מצביעים ל-/jobs/search
    @GetMapping("/search")
    public String searchRedirect(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String category) {
        String kw = (keyword == null ? "" : keyword);
        String cat = (category == null ? "" : category);
        return "redirect:/jobs?keyword=" + org.springframework.web.util.UriUtils.encode(kw, java.nio.charset.StandardCharsets.UTF_8)
                + "&category=" + org.springframework.web.util.UriUtils.encode(cat, java.nio.charset.StandardCharsets.UTF_8);
    }

}


