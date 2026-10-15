package com.movie.api.controller;

import com.movie.collection.entity.MovieNote;
import com.movie.collection.service.MovieNoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notes")
public class MovieNoteController {

    private final MovieNoteService movieNoteService;

    public MovieNoteController(MovieNoteService movieNoteService) {
        this.movieNoteService = movieNoteService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNote(Authentication authentication,
                                        @RequestParam Long movieId,
                                        @RequestParam String title,
                                        @RequestParam String content,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) Boolean isPublic) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            MovieNote note = movieNoteService.createNote(userId, movieId, title, content, category, isPublic);
            return ResponseEntity.ok(createSuccessResponse("笔记创建成功", note));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/update/{noteId}")
    public ResponseEntity<?> updateNote(Authentication authentication,
                                        @PathVariable Long noteId,
                                        @RequestParam(required = false) String title,
                                        @RequestParam(required = false) String content,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) Boolean isPublic,
                                        @RequestParam(required = false) Boolean isFavorite) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            MovieNote note = movieNoteService.updateNote(noteId, userId, title, content, category, isPublic, isFavorite);
            return ResponseEntity.ok(createSuccessResponse("笔记更新成功", note));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/delete/{noteId}")
    public ResponseEntity<?> deleteNote(Authentication authentication, @PathVariable Long noteId) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            movieNoteService.deleteNote(noteId, userId);
            return ResponseEntity.ok(createSuccessResponse("笔记删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getUserNotes(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<MovieNote> notes = movieNoteService.getUserNotes(userId);
        return ResponseEntity.ok(createSuccessResponse("获取成功", notes));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getNotesByMovie(Authentication authentication, @PathVariable Long movieId) {
        Long userId = (Long) authentication.getPrincipal();
        List<MovieNote> notes = movieNoteService.getUserNotesByMovie(userId, movieId);
        return ResponseEntity.ok(createSuccessResponse("获取成功", notes));
    }

    @GetMapping("/public/movie/{movieId}")
    public ResponseEntity<?> getPublicNotesByMovie(@PathVariable Long movieId) {
        List<MovieNote> notes = movieNoteService.getPublicNotesByMovie(movieId);
        return ResponseEntity.ok(createSuccessResponse("获取成功", notes));
    }

    @GetMapping("/favorites")
    public ResponseEntity<?> getFavoriteNotes(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<MovieNote> notes = movieNoteService.getUserFavoriteNotes(userId);
        return ResponseEntity.ok(createSuccessResponse("获取成功", notes));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getNotesByCategory(Authentication authentication, @PathVariable String category) {
        Long userId = (Long) authentication.getPrincipal();
        List<MovieNote> notes = movieNoteService.getUserNotesByCategory(userId, category);
        return ResponseEntity.ok(createSuccessResponse("获取成功", notes));
    }

    @GetMapping("/detail/{noteId}")
    public ResponseEntity<?> getNoteDetail(Authentication authentication, @PathVariable Long noteId) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            MovieNote note = movieNoteService.getNoteById(noteId, userId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", note));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getUserNoteCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long count = movieNoteService.getUserNoteCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return ResponseEntity.ok(createSuccessResponse("获取成功", data));
    }

    @GetMapping("/count/movie/{movieId}")
    public ResponseEntity<?> getUserNoteCountByMovie(Authentication authentication, @PathVariable Long movieId) {
        Long userId = (Long) authentication.getPrincipal();
        Long count = movieNoteService.getUserNoteCountByMovie(userId, movieId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return ResponseEntity.ok(createSuccessResponse("获取成功", data));
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
