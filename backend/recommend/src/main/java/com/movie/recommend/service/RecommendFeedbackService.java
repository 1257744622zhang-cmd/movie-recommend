package com.movie.recommend.service;

import com.movie.recommend.entity.RecommendFeedback;
import java.util.List;

public interface RecommendFeedbackService {
    void addFeedback(Long userId, Long movieId, String feedbackType, Double score);
    List<RecommendFeedback> getUserFeedback(Long userId);
    RecommendFeedback getFeedbackByUserIdAndMovieId(Long userId, Long movieId);
    
    void addDislike(Long userId, Long movieId);
    void removeDislike(Long userId, Long movieId);
    List<RecommendFeedback> getDislikedMovies(Long userId);
    boolean isDisliked(Long userId, Long movieId);
}
