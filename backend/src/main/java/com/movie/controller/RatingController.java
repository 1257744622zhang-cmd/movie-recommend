package com.movie.controller;

import com.movie.entity.Rating;
import com.movie.service.RatingService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ratings")
public class RatingController {
    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public Rating rate(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        Long movieId = Long.valueOf(body.get("movieId").toString());
        Double score = Double.valueOf(body.get("score").toString());
        return ratingService.rate(userId, movieId, score);
    }
}
