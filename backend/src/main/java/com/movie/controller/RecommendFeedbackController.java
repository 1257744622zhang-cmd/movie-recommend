package com.movie.controller;

import com.movie.entity.RecommendFeedback;
import com.movie.service.RecommendFeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recommend-feedback")
public class RecommendFeedbackController {
    private final RecommendFeedbackService feedbackService;

    public RecommendFeedbackController(RecommendFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/dislike")
    public RecommendFeedback dislike(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        Long movieId = Long.valueOf(body.get("movieId").toString());
        return feedbackService.addDislike(userId, movieId);
    }

    @DeleteMapping("/dislike/{movieId}")
    public void removeDislike(@RequestHeader("X-User-Id") Long userId, @PathVariable Long movieId) {
        feedbackService.removeDislike(userId, movieId);
    }

    @GetMapping("/disliked")
    public List<RecommendFeedback> getDisliked(@RequestHeader("X-User-Id") Long userId) {
        return feedbackService.getDislikedMovies(userId);
    }
}
