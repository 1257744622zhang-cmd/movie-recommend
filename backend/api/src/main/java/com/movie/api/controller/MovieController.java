package com.movie.api.controller;

import com.movie.movie.dto.MovieVO;
import com.movie.movie.entity.Movie;
import com.movie.movie.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/movie")
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<MovieVO>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/hot")
    public ResponseEntity<List<MovieVO>> getHotMovies() {
        return ResponseEntity.ok(movieService.getHotMovies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieVO> getMovieById(@PathVariable Long id) {
        MovieVO movie = movieService.getMovieById(id);
        if (movie == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(movie);
    }

    @PostMapping
    public ResponseEntity<MovieVO> addMovie(@RequestBody Movie movie) {
        return ResponseEntity.ok(movieService.saveMovie(movie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieVO> updateMovie(@PathVariable Long id, @RequestBody Movie movie) {
        MovieVO existingMovie = movieService.getMovieById(id);
        if (existingMovie == null) {
            return ResponseEntity.notFound().build();
        }
        movie.setId(id);
        return ResponseEntity.ok(movieService.saveMovie(movie));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieVO>> searchMovies(@RequestParam String keyword) {
        return ResponseEntity.ok(movieService.searchMovies(keyword));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<MovieVO>> filterMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) String actor) {
        return ResponseEntity.ok(movieService.filterMovies(genre, director, actor));
    }

    @GetMapping("/filter-advanced")
    public List<MovieVO> filterMoviesAdvanced(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) String region) {
        System.out.println("高级筛选参数: genre=" + genre + ", minRating=" + minRating + 
                          ", startYear=" + startYear + ", endYear=" + endYear + ", region=" + region);
        List<MovieVO> result = movieService.filterMoviesAdvanced(genre, minRating, startYear, endYear, region);
        System.out.println("筛选结果数量: " + (result != null ? result.size() : 0));
        return result;
    }
}
