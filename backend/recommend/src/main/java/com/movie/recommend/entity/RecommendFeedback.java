package com.movie.recommend.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "recommend_feedback")
public class RecommendFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long movieId;
    @Column(length = 20)
    private String feedbackType; // DISLIKE, LIKE, CLICK, WATCH
    private Double score;
}
