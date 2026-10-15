package com.movie.api.controller;

import com.movie.collection.entity.WatchStatus;
import com.movie.collection.service.WatchStatusService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/watch-status")
public class WatchStatusController {

    private final WatchStatusService watchStatusService;

    public WatchStatusController(WatchStatusService watchStatusService) {
        this.watchStatusService = watchStatusService;
    }

    @PostMapping("/{movieId}")
    public WatchStatus setWatchStatus(Authentication authentication,
                                     @PathVariable Long movieId,
                                     @RequestBody Map<String, String> body) {
        Long userId = (Long) authentication.getPrincipal();
        String status = body.get("status");
        return watchStatusService.setWatchStatus(userId, movieId, status);
    }

    @GetMapping("/{movieId}")
    public Map<String, String> getWatchStatus(Authentication authentication, @PathVariable Long movieId) {
        Long userId = (Long) authentication.getPrincipal();
        String status = watchStatusService.getWatchStatus(userId, movieId);
        Map<String, String> result = new HashMap<>();
        result.put("status", status);
        return result;
    }

    @DeleteMapping("/{movieId}")
    public void removeWatchStatus(Authentication authentication, @PathVariable Long movieId) {
        Long userId = (Long) authentication.getPrincipal();
        watchStatusService.removeWatchStatus(userId, movieId);
    }

    @GetMapping("/list")
    public List<WatchStatus> getUserWatchStatuses(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return watchStatusService.getUserWatchStatuses(userId);
    }
}
