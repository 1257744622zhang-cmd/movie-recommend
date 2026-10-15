package com.movie.controller;

import com.movie.dto.MovieVO;
import com.movie.entity.Movie;
import com.movie.service.CollectionService;
import com.movie.service.MovieService;
import com.movie.service.RatingService;
import com.movie.service.RecommendFeedbackService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;
    private final RatingService ratingService;
    private final CollectionService collectionService;
    private final RecommendFeedbackService feedbackService;

    public MovieController(MovieService movieService, RatingService ratingService, CollectionService collectionService, RecommendFeedbackService feedbackService) {
        this.movieService = movieService;
        this.ratingService = ratingService;
        this.collectionService = collectionService;
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public Page<MovieVO> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Page<Movie> p = movieService.search(keyword, genre, yearFrom, yearTo, region, page, size);
        return p.map(m -> {
            MovieVO v = MovieVO.from(m);
            if (userId != null) {
                ratingService.getMyScore(userId, m.getId()).ifPresent(v::setMyScore);
                // 检查收藏状态
                boolean isCollected = collectionService.getByUserIdAndCollectionFolder(userId, "默认收藏夹").stream()
                        .anyMatch(c -> c.getMovieId().equals(m.getId()));
                v.setIsCollected(isCollected);
                // 检查不喜欢状态
                boolean isDisliked = feedbackService.isDisliked(userId, m.getId());
                v.setIsDisliked(isDisliked);
            }
            return v;
        });
    }

    @GetMapping("/{id}")
    public MovieVO getById(@PathVariable Long id,
                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Movie m = movieService.getById(id).orElseThrow(() -> new RuntimeException("电影不存在"));
        MovieVO v = MovieVO.from(m);
        if (userId != null) {
            ratingService.getMyScore(userId, id).ifPresent(v::setMyScore);
            // 检查收藏状态
                boolean isCollected = collectionService.getByUserIdAndCollectionFolder(userId, "默认收藏夹").stream()
                        .anyMatch(c -> c.getMovieId().equals(m.getId()));
                v.setIsCollected(isCollected);
            // 检查不喜欢状态
            boolean isDisliked = feedbackService.isDisliked(userId, m.getId());
            v.setIsDisliked(isDisliked);
        }
        return v;
    }

    @PostMapping
    public MovieVO add(@RequestBody Movie movie,
                       @RequestHeader(value = "X-User-Id", required = false) Long userId,
                       @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("需要管理员权限");
        return MovieVO.from(movieService.save(movie));
    }

    @PutMapping("/{id}")
    public MovieVO update(@PathVariable Long id, @RequestBody Movie movie,
                          @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("需要管理员权限");
        movie.setId(id);
        return MovieVO.from(movieService.save(movie));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id,
                       @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("需要管理员权限");
        movieService.delete(id);
    }
}
