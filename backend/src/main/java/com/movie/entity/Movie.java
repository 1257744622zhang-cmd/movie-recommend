package com.movie.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(length = 100)
    private String director;
    @Column(length = 500)
    private String actors;
    @Column(length = 100)
    private String genre;
    private Integer releaseYear;
    @Column(length = 50)
    private String region;
    @Column(columnDefinition = "TEXT")
    private String description;
    private Double avgRating = 0.0;
    private Integer ratingCount = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
}
