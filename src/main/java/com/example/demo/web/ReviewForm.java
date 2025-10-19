package com.example.demo.web;

import jakarta.validation.constraints.*;

public class ReviewForm {
    @NotNull @Min(1) @Max(5)
    private Integer rating;

    @NotBlank @Size(max=150)
    private String reviewerName;

    @Size(max=4000)
    private String comment;

    // getters/setters
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
