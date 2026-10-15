package com.movie.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
public class RecommendFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long movieId;
    private String feedbackType; // dislike, like
    private LocalDateTime createdAt;

    public RecommendFeedback() {
        this.createdAt = LocalDateTime.now();
    }

    public RecommendFeedback(Long userId, Long movieId, String feedbackType) {
        this.userId = userId;
        this.movieId = movieId;
        this.feedbackType = feedbackType;
        this.createdAt = LocalDateTime.now();
    }
}
