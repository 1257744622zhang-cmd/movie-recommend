package com.movie.api.controller;

import com.movie.recommend.entity.RecommendFeedback;
import com.movie.recommend.service.RecommendFeedbackService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommend-feedback")
public class RecommendFeedbackController {
    private final RecommendFeedbackService recommendFeedbackService;

    public RecommendFeedbackController(RecommendFeedbackService recommendFeedbackService) {
        this.recommendFeedbackService = recommendFeedbackService;
    }

    @PostMapping("/add")
    public void addFeedback(Authentication authentication, 
                           @RequestParam Long movieId, 
                           @RequestParam String feedbackType, 
                           @RequestParam(required = false) Double score) {
        Long userId = (Long) authentication.getPrincipal();
        recommendFeedbackService.addFeedback(userId, movieId, feedbackType, score);
    }

    @GetMapping("/user")
    public List<RecommendFeedback> getUserFeedback(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return recommendFeedbackService.getUserFeedback(userId);
    }

    @GetMapping("/check")
    public RecommendFeedback getFeedbackByUserIdAndMovieId(Authentication authentication, @RequestParam Long movieId) {
        Long userId = (Long) authentication.getPrincipal();
        return recommendFeedbackService.getFeedbackByUserIdAndMovieId(userId, movieId);
    }

    @PostMapping("/dislike")
    public void addDislike(Authentication authentication, @RequestParam Long movieId) {
        Long userId = (Long) authentication.getPrincipal();
        recommendFeedbackService.addDislike(userId, movieId);
    }

    @DeleteMapping("/dislike")
    public void removeDislike(Authentication authentication, @RequestParam Long movieId) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("用户未登录");
        }
        Long userId = (Long) authentication.getPrincipal();
        System.out.println("取消不推荐 - userId: " + userId + ", movieId: " + movieId);
        recommendFeedbackService.removeDislike(userId, movieId);
    }

    @GetMapping("/disliked")
    public List<RecommendFeedback> getDislikedMovies(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return recommendFeedbackService.getDislikedMovies(userId);
    }
}
