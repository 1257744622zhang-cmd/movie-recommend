package com.movie.api.controller;

import com.movie.collection.entity.Tag;
import com.movie.collection.service.MovieTagService;
import com.movie.movie.dto.MovieVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movie-tag")
public class MovieTagController {

    private final MovieTagService movieTagService;

    public MovieTagController(MovieTagService movieTagService) {
        this.movieTagService = movieTagService;
    }

    @PostMapping("/add")
    public void addTagToMovie(@RequestBody Map<String, Long> body) {
        Long movieId = body.get("movieId");
        Long tagId = body.get("tagId");
        movieTagService.addTagToMovie(movieId, tagId);
    }

    @PostMapping("/add-batch")
    public void addTagsToMovie(@RequestBody Map<String, Object> body) {
        Long movieId = Long.valueOf(body.get("movieId").toString());
        @SuppressWarnings("unchecked")
        List<Long> tagIds = (List<Long>) body.get("tagIds");
        movieTagService.addTagsToMovie(movieId, tagIds);
    }

    @DeleteMapping("/remove")
    public void removeTagFromMovie(@RequestParam Long movieId, @RequestParam Long tagId) {
        movieTagService.removeTagFromMovie(movieId, tagId);
    }

    @GetMapping("/movie/{movieId}")
    public List<Tag> getTagsByMovieId(@PathVariable Long movieId) {
        return movieTagService.getTagsByMovieId(movieId);
    }

    @GetMapping("/tag/{tagId}/movies")
    public List<MovieVO> getMoviesByTagId(@PathVariable Long tagId) {
        return movieTagService.searchMoviesByTag(tagId);
    }

    @PostMapping("/search-and")
    public List<MovieVO> searchMoviesByTagsAnd(@RequestBody Map<String, List<Long>> body) {
        List<Long> tagIds = body.get("tagIds");
        return movieTagService.searchMoviesByTagsAnd(tagIds);
    }

    @PostMapping("/search-or")
    public List<MovieVO> searchMoviesByTagsOr(@RequestBody Map<String, List<Long>> body) {
        List<Long> tagIds = body.get("tagIds");
        return movieTagService.searchMoviesByTagsOr(tagIds);
    }

    @GetMapping("/check")
    public boolean hasTag(@RequestParam Long movieId, @RequestParam Long tagId) {
        return movieTagService.hasTag(movieId, tagId);
    }

    @GetMapping("/movie/{movieId}/count")
    public long getMovieTagCount(@PathVariable Long movieId) {
        return movieTagService.getMovieTagCount(movieId);
    }
}
