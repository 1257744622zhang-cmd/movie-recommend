package com.movie.dto;

import com.movie.entity.Movie;
import lombok.Data;

@Data
public class MovieVO {
    private Long id;
    private String title;
    private String director;
    private String actors;
    private String genre;
    private Integer releaseYear;
    private String region;
    private String description;
    private Double avgRating;
    private Integer ratingCount;
    private Double myScore; // 当前用户评分，可选
    private Boolean isCollected; // 当前用户是否收藏，可选
    private Boolean isDisliked; // 当前用户是否不喜欢，可选

    public static MovieVO from(Movie m) {
        MovieVO v = new MovieVO();
        v.setId(m.getId());
        v.setTitle(m.getTitle());
        v.setDirector(m.getDirector());
        v.setActors(m.getActors());
        v.setGenre(m.getGenre());
        v.setReleaseYear(m.getReleaseYear());
        v.setRegion(m.getRegion());
        v.setDescription(m.getDescription());
        v.setAvgRating(m.getAvgRating());
        v.setRatingCount(m.getRatingCount());
        return v;
    }
}
