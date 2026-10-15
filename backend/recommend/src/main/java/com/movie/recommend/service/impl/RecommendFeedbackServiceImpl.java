package com.movie.recommend.service.impl;

import com.movie.recommend.entity.RecommendFeedback;
import com.movie.recommend.repository.RecommendFeedbackRepository;
import com.movie.recommend.service.RecommendFeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecommendFeedbackServiceImpl implements RecommendFeedbackService {
    private final RecommendFeedbackRepository recommendFeedbackRepository;
    private static final String FEEDBACK_TYPE_DISLIKE = "DISLIKE";

    public RecommendFeedbackServiceImpl(RecommendFeedbackRepository recommendFeedbackRepository) {
        this.recommendFeedbackRepository = recommendFeedbackRepository;
    }

    @Override
    public void addFeedback(Long userId, Long movieId, String feedbackType, Double score) {
        RecommendFeedback existingFeedback = recommendFeedbackRepository.findByUserIdAndMovieId(userId, movieId);
        if (existingFeedback != null) {
            existingFeedback.setFeedbackType(feedbackType != null ? feedbackType : existingFeedback.getFeedbackType());
            existingFeedback.setScore(score != null ? score : existingFeedback.getScore());
            recommendFeedbackRepository.save(existingFeedback);
        } else {
            RecommendFeedback feedback = new RecommendFeedback();
            feedback.setUserId(userId);
            feedback.setMovieId(movieId);
            feedback.setFeedbackType(feedbackType);
            feedback.setScore(score);
            recommendFeedbackRepository.save(feedback);
        }
    }

    @Override
    public List<RecommendFeedback> getUserFeedback(Long userId) {
        return recommendFeedbackRepository.findByUserId(userId);
    }

    @Override
    public RecommendFeedback getFeedbackByUserIdAndMovieId(Long userId, Long movieId) {
        return recommendFeedbackRepository.findByUserIdAndMovieId(userId, movieId);
    }

    @Override
    @Transactional
    public void addDislike(Long userId, Long movieId) {
        RecommendFeedback existing = recommendFeedbackRepository.findByUserIdAndMovieIdAndFeedbackType(userId, movieId, FEEDBACK_TYPE_DISLIKE);
        if (existing == null) {
            RecommendFeedback feedback = new RecommendFeedback();
            feedback.setUserId(userId);
            feedback.setMovieId(movieId);
            feedback.setFeedbackType(FEEDBACK_TYPE_DISLIKE);
            recommendFeedbackRepository.save(feedback);
        }
    }

    @Override
    @Transactional
    public void removeDislike(Long userId, Long movieId) {
        recommendFeedbackRepository.deleteByUserIdAndMovieIdAndFeedbackType(userId, movieId, FEEDBACK_TYPE_DISLIKE);
    }

    @Override
    public List<RecommendFeedback> getDislikedMovies(Long userId) {
        return recommendFeedbackRepository.findByUserIdAndFeedbackType(userId, FEEDBACK_TYPE_DISLIKE);
    }

    @Override
    public boolean isDisliked(Long userId, Long movieId) {
        return recommendFeedbackRepository.findByUserIdAndMovieIdAndFeedbackType(userId, movieId, FEEDBACK_TYPE_DISLIKE) != null;
    }
}
