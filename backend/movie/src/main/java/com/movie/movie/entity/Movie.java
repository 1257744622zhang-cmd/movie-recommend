package com.movie.movie.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String title;
    @Column(length = 50)
    private String genre;
    @Column(length = 50)
    private String director;
    @Column(length = 100)
    private String actors;
    @Column(name = "release_year")
    private Integer releaseYear;
    @Column(length = 50)
    private String region;
    @Column(length = 1000)
    private String description;
    @Column(name = "avg_rating")
    private Double avgRating;
    @Column(name = "rating_count")
    private Integer ratingCount;
    @Column(length = 255)
    private String poster;
}
