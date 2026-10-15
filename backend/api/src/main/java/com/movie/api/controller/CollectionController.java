package com.movie.api.controller;

import com.movie.collection.service.CollectionService;
import com.movie.movie.dto.MovieVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collection")
public class CollectionController {
    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping("/add")
    public void addToCollection(Authentication authentication, 
                               @RequestParam Long movieId, 
                               @RequestParam(required = false) String status, 
                               @RequestParam(required = false) String collectionName, 
                               @RequestParam(required = false) String permission) {
        Long userId = (Long) authentication.getPrincipal();
        collectionService.addToCollection(userId, movieId, status, collectionName, permission);
    }

    @DeleteMapping("/remove")
    public void removeFromCollection(Authentication authentication, @RequestParam Long movieId) {
        Long userId = (Long) authentication.getPrincipal();
        collectionService.removeFromCollection(userId, movieId);
    }

    @PutMapping("/status")
    public void updateCollectionStatus(Authentication authentication, 
                                      @RequestParam Long movieId, 
                                      @RequestParam String status) {
        Long userId = (Long) authentication.getPrincipal();
        collectionService.updateStatus(userId, movieId, status);
    }

    @GetMapping("/user")
    public List<MovieVO> getUserCollections(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return collectionService.getUserCollections(userId);
    }

    @GetMapping("/user/status")
    public List<MovieVO> getUserCollectionByStatus(Authentication authentication, @RequestParam String status) {
        Long userId = (Long) authentication.getPrincipal();
        return collectionService.getUserCollectionByStatus(userId, status);
    }

    @GetMapping("/user/collection")
    public List<MovieVO> getUserCollectionByCollectionName(Authentication authentication, @RequestParam String collectionName) {
        Long userId = (Long) authentication.getPrincipal();
        return collectionService.getUserCollectionByCollectionName(userId, collectionName);
    }

    @GetMapping("/check")
    public boolean isMovieCollected(Authentication authentication, @RequestParam Long movieId) {
        Long userId = (Long) authentication.getPrincipal();
        return collectionService.isMovieCollected(userId, movieId);
    }
}
