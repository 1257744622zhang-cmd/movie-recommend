package com.movie.recommend.service.impl;

import com.movie.movie.entity.Movie;
import com.movie.movie.repository.MovieRepository;
import com.movie.recommend.dto.RatingDTO;
import com.movie.recommend.dto.RatingStatsDTO;
import com.movie.recommend.entity.Rating;
import com.movie.recommend.repository.RatingRepository;
import com.movie.recommend.service.RatingService;
import com.movie.user.entity.User;
import com.movie.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public RatingServiceImpl(RatingRepository ratingRepository, UserRepository userRepository, MovieRepository movieRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    public void addRating(Long userId, Long movieId, Double rating, String note) {
        // 验证评分范围 0-10分
        if (rating < 0 || rating > 10) {
            throw new IllegalArgumentException("评分必须在0-10分之间");
        }
        
        // 将评分精确到0.5分
        rating = Math.round(rating * 2) / 2.0;
        
        Rating existingRating = ratingRepository.findByUserIdAndMovieId(userId, movieId);
        if (existingRating != null) {
            existingRating.setRating(rating);
            existingRating.setNote(note != null ? note : existingRating.getNote());
            ratingRepository.save(existingRating);
        } else {
            Rating newRating = new Rating();
            newRating.setUserId(userId);
            newRating.setMovieId(movieId);
            newRating.setRating(rating);
            newRating.setNote(note);
            ratingRepository.save(newRating);
        }
        
        // 更新电影的评分信息
        updateMovieRatingInfo(movieId);
    }
    
    private void updateMovieRatingInfo(Long movieId) {
        List<Rating> ratings = ratingRepository.findByMovieId(movieId);
        double averageRating = 0.0;
        int ratingCount = ratings.size();
        
        if (!ratings.isEmpty()) {
            double sum = ratings.stream().mapToDouble(Rating::getRating).sum();
            averageRating = BigDecimal.valueOf(sum / ratings.size())
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        
        // 更新电影信息
        Movie movie = movieRepository.findById(movieId).orElse(null);
        if (movie != null) {
            movie.setAvgRating(averageRating);
            movie.setRatingCount(ratingCount);
            movieRepository.save(movie);
        }
    }

    @Override
    public List<Rating> getUserRatings(Long userId) {
        return ratingRepository.findByUserId(userId);
    }

    @Override
    public List<RatingDTO> getMovieRatings(Long movieId) {
        List<Rating> ratings = ratingRepository.findByMovieId(movieId);
        return ratings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Rating getRatingByUserIdAndMovieId(Long userId, Long movieId) {
        return ratingRepository.findByUserIdAndMovieId(userId, movieId);
    }

    @Override
    public Double getAverageRating(Long movieId) {
        List<Rating> ratings = ratingRepository.findByMovieId(movieId);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double sum = ratings.stream().mapToDouble(Rating::getRating).sum();
        return BigDecimal.valueOf(sum / ratings.size())
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public RatingStatsDTO getRatingStats(Long movieId) {
        RatingStatsDTO stats = new RatingStatsDTO();
        stats.setMovieId(movieId);
        stats.setAverageRating(getAverageRating(movieId));
        
        List<Rating> ratings = ratingRepository.findByMovieId(movieId);
        stats.setTotalCount((long) ratings.size());
        stats.setDistribution(getRatingDistribution(movieId));
        
        return stats;
    }

    @Override
    public Map<Integer, Long> getRatingDistribution(Long movieId) {
        List<Rating> ratings = ratingRepository.findByMovieId(movieId);
        Map<Integer, Long> distribution = new HashMap<>();
        
        // 初始化0-10分的分布
        for (int i = 0; i <= 10; i++) {
            distribution.put(i, 0L);
        }
        
        // 统计分布
        for (Rating rating : ratings) {
            int score = rating.getRating().intValue();
            distribution.put(score, distribution.get(score) + 1);
        }
        
        return distribution;
    }

    private RatingDTO convertToDTO(Rating rating) {
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setUserId(rating.getUserId());
        dto.setMovieId(rating.getMovieId());
        dto.setRating(rating.getRating());
        dto.setNote(rating.getNote());
        dto.setCreatedAt(rating.getCreatedAt());
        dto.setUpdatedAt(rating.getUpdatedAt());
        
        // 获取用户名
        User user = userRepository.findById(rating.getUserId()).orElse(null);
        if (user != null) {
            dto.setUsername(user.getUsername());
        }
        
        return dto;
    }
}
