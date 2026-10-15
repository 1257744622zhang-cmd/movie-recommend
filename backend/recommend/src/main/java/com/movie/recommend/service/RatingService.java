package com.movie.recommend.service;

import com.movie.recommend.dto.RatingDTO;
import com.movie.recommend.dto.RatingStatsDTO;
import com.movie.recommend.entity.Rating;
import java.util.List;
import java.util.Map;

public interface RatingService {
    void addRating(Long userId, Long movieId, Double rating, String note);
    List<Rating> getUserRatings(Long userId);
    List<RatingDTO> getMovieRatings(Long movieId);
    Rating getRatingByUserIdAndMovieId(Long userId, Long movieId);
    Double getAverageRating(Long movieId);
    RatingStatsDTO getRatingStats(Long movieId);
    Map<Integer, Long> getRatingDistribution(Long movieId);
}