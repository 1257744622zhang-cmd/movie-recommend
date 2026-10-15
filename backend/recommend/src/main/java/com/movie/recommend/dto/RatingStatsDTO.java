package com.movie.recommend.dto;

import lombok.Data;
import java.util.Map;

@Data
public class RatingStatsDTO {
    private Long movieId;
    private Double averageRating;
    private Long totalCount;
    private Map<Integer, Long> distribution;
    private Double myRating;
}
