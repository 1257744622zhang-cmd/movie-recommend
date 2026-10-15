package com.movie.service;

import com.movie.entity.RecommendFeedback;

import java.util.List;

public interface RecommendFeedbackService {
    RecommendFeedback addDislike(Long userId, Long movieId);
    void removeDislike(Long userId, Long movieId);
    List<RecommendFeedback> getDislikedMovies(Long userId);
    boolean isDisliked(Long userId, Long movieId);
    List<Long> getDislikedMovieIds(Long userId);
}
