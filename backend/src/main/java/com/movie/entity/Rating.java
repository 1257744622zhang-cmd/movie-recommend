package com.movie.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ratings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "movie_id", nullable = false)
    private Long movieId;
    private Double score; // 0-10
    private LocalDateTime createdAt = LocalDateTime.now();
}
