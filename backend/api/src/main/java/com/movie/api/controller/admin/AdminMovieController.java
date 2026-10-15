package com.movie.api.controller.admin;

import com.movie.collection.service.TagService;
import com.movie.movie.dto.MovieVO;
import com.movie.movie.entity.Movie;
import com.movie.movie.service.MovieService;
import com.movie.recommend.entity.Rating;
import com.movie.recommend.repository.RatingRepository;
import com.movie.recommend.service.RatingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/movie")
public class AdminMovieController {

    private final MovieService movieService;
    private final RatingRepository ratingRepository;
    private final TagService tagService;

    public AdminMovieController(MovieService movieService,
                               RatingRepository ratingRepository,
                               TagService tagService) {
        this.movieService = movieService;
        this.ratingRepository = ratingRepository;
        this.tagService = tagService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(createSuccessResponse("获取成功", movieService.getAllMoviesPageable(pageRequest)));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllMovies() {
        return ResponseEntity.ok(createSuccessResponse("获取成功", movieService.getAllMovies()));
    }

    @PostMapping("")
    public ResponseEntity<?> addMovie(@RequestBody Movie movie) {
        MovieVO savedMovie = movieService.saveMovie(movie);
        return ResponseEntity.ok(createSuccessResponse("添加成功", savedMovie));
    }

    @PostMapping("/batch-import")
    public ResponseEntity<?> batchImportMovies(@RequestParam("file") MultipartFile file) {
        try {
            List<Movie> movies = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            boolean isFirst = true;
            while ((line = reader.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    Movie movie = new Movie();
                    movie.setTitle(parts[0].trim());
                    movie.setGenre(parts[1].trim());
                    movie.setDirector(parts[2].trim());
                    movie.setActors(parts[3].trim());
                    movie.setReleaseYear(Integer.parseInt(parts[4].trim()));
                    movie.setRegion(parts[5].trim());
                    if (parts.length > 6) {
                        movie.setDescription(parts[6].trim());
                    }
                    movies.add(movie);
                }
            }
            movies.forEach(movieService::saveMovie);
            return ResponseEntity.ok(createSuccessResponse("批量导入成功，共导入 " + movies.size() + " 部电影", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("批量导入失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMovie(@PathVariable Long id, @RequestBody Movie movie) {
        movie.setId(id);
        MovieVO updatedMovie = movieService.saveMovie(movie);
        return ResponseEntity.ok(createSuccessResponse("更新成功", updatedMovie));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(createSuccessResponse("删除成功", null));
    }

    @GetMapping("/abnormal-ratings")
    public ResponseEntity<?> getAbnormalRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Rating> ratings = ratingRepository.findAll(pageRequest);

        List<Rating> abnormalRatings = new ArrayList<>();
        for (Rating rating : ratings.getContent()) {
            if (isAbnormalRating(rating)) {
                abnormalRatings.add(rating);
            }
        }
        return ResponseEntity.ok(createSuccessResponse("获取异常评分成功", abnormalRatings));
    }

    @DeleteMapping("/rating/{id}")
    public ResponseEntity<?> deleteRating(@PathVariable Long id) {
        ratingRepository.deleteById(id);
        return ResponseEntity.ok(createSuccessResponse("删除评分成功", null));
    }

    @PostMapping("/tags/batch")
    public ResponseEntity<?> batchManageTags(@RequestBody Map<String, Object> request) {
        Long movieId = Long.valueOf(request.get("movieId").toString());
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) request.get("tags");
        tagService.batchAddTagsToMovie(movieId, tags);
        return ResponseEntity.ok(createSuccessResponse("批量标签管理成功", null));
    }

    private boolean isAbnormalRating(Rating rating) {
        return rating.getRating() < 1 || rating.getRating() > 10;
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
