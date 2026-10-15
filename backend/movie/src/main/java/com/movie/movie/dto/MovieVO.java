package com.movie.movie.dto;

import lombok.Data;

@Data
public class MovieVO {
    private Long id;
    private String title;
    private String genre;
    private String director;
    private String actors;
    private Integer releaseYear;
    private String region;
    private String description;
    private Double avgRating;
    private Integer ratingCount;
    private String poster;
    private Double myScore;
    private Boolean isCollected; // 是否已收藏（收藏状态）
    private Boolean isDisliked;
    private String watchStatus; // 观看状态: WATCHLIST, WATCHING, WATCHED, NONE
    private String status; // 用于收藏列表中的状态
}
