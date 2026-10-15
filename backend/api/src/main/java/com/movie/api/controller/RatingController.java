package com.movie.api.controller;

import com.movie.recommend.dto.RatingDTO;
import com.movie.recommend.dto.RatingStatsDTO;
import com.movie.recommend.entity.Rating;
import com.movie.recommend.service.RatingService;
import com.movie.user.service.UserBehaviorLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rating")
public class RatingController {
    private final RatingService ratingService;
    private final UserBehaviorLogService userBehaviorLogService;

    public RatingController(RatingService ratingService, UserBehaviorLogService userBehaviorLogService) {
        this.ratingService = ratingService;
        this.userBehaviorLogService = userBehaviorLogService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addRating(Authentication authentication, 
                         @RequestParam Long movieId, 
                         @RequestParam Double rating, 
                         @RequestParam(required = false) String note) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            ratingService.addRating(userId, movieId, rating, note);
            
            // 记录评分行为
            userBehaviorLogService.logBehavior(userId, "RATING", movieId, String.valueOf(rating));
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "评分成功");
            result.put("rating", Math.round(rating * 2) / 2.0); // 返回实际保存的评分
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/user")
    public List<Rating> getUserRatings(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ratingService.getUserRatings(userId);
    }

    @GetMapping("/movie")
    public List<RatingDTO> getMovieRatings(@RequestParam Long movieId) {
        return ratingService.getMovieRatings(movieId);
    }

    @GetMapping("/check")
    public Rating getRatingByUserIdAndMovieId(Authentication authentication, @RequestParam Long movieId) {
        Long userId = (Long) authentication.getPrincipal();
        return ratingService.getRatingByUserIdAndMovieId(userId, movieId);
    }

    @GetMapping("/average")
    public Double getAverageRating(@RequestParam Long movieId) {
        return ratingService.getAverageRating(movieId);
    }

    @GetMapping("/stats")
    public RatingStatsDTO getRatingStats(@RequestParam Long movieId) {
        return ratingService.getRatingStats(movieId);
    }

    @GetMapping("/distribution")
    public Map<Integer, Long> getRatingDistribution(@RequestParam Long movieId) {
        return ratingService.getRatingDistribution(movieId);
    }
}
