package com.movie.recommend.repository;

import com.movie.recommend.entity.RecommendFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RecommendFeedbackRepository extends JpaRepository<RecommendFeedback, Long> {
    List<RecommendFeedback> findByUserId(Long userId);
    List<RecommendFeedback> findByMovieId(Long movieId);
    RecommendFeedback findByUserIdAndMovieId(Long userId, Long movieId);
    List<RecommendFeedback> findByUserIdAndFeedbackType(Long userId, String feedbackType);
    RecommendFeedback findByUserIdAndMovieIdAndFeedbackType(Long userId, Long movieId, String feedbackType);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RecommendFeedback f WHERE f.userId = :userId AND f.movieId = :movieId AND f.feedbackType = :feedbackType")
    void deleteByUserIdAndMovieIdAndFeedbackType(@Param("userId") Long userId, @Param("movieId") Long movieId, @Param("feedbackType") String feedbackType);
}
