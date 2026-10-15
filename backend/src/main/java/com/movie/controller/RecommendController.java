package com.movie.controller;

import com.movie.dto.MovieVO;
import com.movie.entity.Movie;
import com.movie.service.CollectionService;
import com.movie.service.RatingService;
import com.movie.service.RecommendService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recommend")
public class RecommendController {
    private final RecommendService recommendService;
    private final RatingService ratingService;
    private final CollectionService collectionService;

    public RecommendController(RecommendService recommendService, RatingService ratingService, CollectionService collectionService) {
        this.recommendService = recommendService;
        this.ratingService = ratingService;
        this.collectionService = collectionService;
    }

    @GetMapping
    public List<MovieVO> recommend(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        List<Movie> list;
        if (userId == null || ratingService.getRatedMovieIds(userId).size() < 3) {
            list = recommendService.recommendForNewUser(userId);
        } else {
            list = recommendService.recommendForUser(userId);
        }
        return list.stream().map(m -> {
            MovieVO v = MovieVO.from(m);
            if (userId != null) {
                // 检查收藏状态
                boolean isCollected = collectionService.getByUserIdAndCollectionFolder(userId, "默认收藏夹").stream()
                        .anyMatch(c -> c.getMovieId().equals(m.getId()));
                v.setIsCollected(isCollected);
                // 检查不喜欢状态
                boolean isDisliked = recommendService.isDisliked(userId, m.getId());
                v.setIsDisliked(isDisliked);
            }
            return v;
        }).collect(Collectors.toList());
    }
}
