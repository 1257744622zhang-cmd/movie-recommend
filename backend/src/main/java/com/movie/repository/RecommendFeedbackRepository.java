package com.movie.repository;

import com.movie.entity.RecommendFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendFeedbackRepository extends JpaRepository<RecommendFeedback, Long> {
    List<RecommendFeedback> findByUserIdAndFeedbackType(Long userId, String feedbackType);
    boolean existsByUserIdAndMovieIdAndFeedbackType(Long userId, Long movieId, String feedbackType);
}
