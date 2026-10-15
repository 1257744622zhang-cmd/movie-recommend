package com.movie.api.controller;

import com.movie.collection.service.CollectionService;
import com.movie.collection.service.WatchStatusService;
import com.movie.movie.dto.MovieVO;
import com.movie.recommend.service.RecommendService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recommend")
public class RecommendController {
    private final RecommendService recommendService;
    private final CollectionService collectionService;
    private final WatchStatusService watchStatusService;

    public RecommendController(RecommendService recommendService, 
                              CollectionService collectionService,
                              WatchStatusService watchStatusService) {
        this.recommendService = recommendService;
        this.collectionService = collectionService;
        this.watchStatusService = watchStatusService;
    }

    @GetMapping("/user")
    public List<MovieVO> getUserRecommendations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<MovieVO> movies = recommendService.getRecommendations(userId);
        // 设置收藏状态和观看状态（独立的）
        for (MovieVO movie : movies) {
            if (movie != null) {
                // 设置收藏状态
                movie.setIsCollected(collectionService.isMovieCollected(userId, movie.getId()));
                // 设置独立的观看状态
                String watchStatus = watchStatusService.getWatchStatus(userId, movie.getId());
                movie.setWatchStatus(watchStatus != null ? watchStatus : "NONE");
            }
        }
        return movies;
    }

    @GetMapping("/new-user")
    public List<MovieVO> getNewUserRecommendations() {
        return recommendService.getNewUserRecommendations();
    }

    @PostMapping("/refresh")
    public List<MovieVO> refreshRecommendations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<MovieVO> movies = recommendService.refreshRecommendations(userId);
        // 设置收藏状态和观看状态（独立的）
        for (MovieVO movie : movies) {
            if (movie != null) {
                // 设置收藏状态
                movie.setIsCollected(collectionService.isMovieCollected(userId, movie.getId()));
                // 设置独立的观看状态
                String watchStatus = watchStatusService.getWatchStatus(userId, movie.getId());
                movie.setWatchStatus(watchStatus != null ? watchStatus : "NONE");
            }
        }
        return movies;
    }

    @GetMapping("/cf")
    public List<MovieVO> getCFRecommendations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<MovieVO> movies = recommendService.getCFRecommendations(userId);
        // 设置收藏状态和观看状态
        for (MovieVO movie : movies) {
            if (movie != null) {
                movie.setIsCollected(collectionService.isMovieCollected(userId, movie.getId()));
                String watchStatus = watchStatusService.getWatchStatus(userId, movie.getId());
                movie.setWatchStatus(watchStatus != null ? watchStatus : "NONE");
            }
        }
        return movies;
    }

    @GetMapping("/autoencoder")
    public List<MovieVO> getAutoencoderRecommendations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<MovieVO> movies = recommendService.getAutoencoderRecommendations(userId);
        // 设置收藏状态和观看状态
        for (MovieVO movie : movies) {
            if (movie != null) {
                movie.setIsCollected(collectionService.isMovieCollected(userId, movie.getId()));
                String watchStatus = watchStatusService.getWatchStatus(userId, movie.getId());
                movie.setWatchStatus(watchStatus != null ? watchStatus : "NONE");
            }
        }
        return movies;
    }

    @GetMapping("/hybrid")
    public List<MovieVO> getHybridRecommendations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<MovieVO> movies = recommendService.getHybridRecommendations(userId);
        // 设置收藏状态和观看状态
        for (MovieVO movie : movies) {
            if (movie != null) {
                movie.setIsCollected(collectionService.isMovieCollected(userId, movie.getId()));
                String watchStatus = watchStatusService.getWatchStatus(userId, movie.getId());
                movie.setWatchStatus(watchStatus != null ? watchStatus : "NONE");
            }
        }
        return movies;
    }

    @GetMapping("/profile")
    public Map<String, Object> getUserProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Map<String, Object> profile = new java.util.HashMap<>();
        profile.put("interestFeatures", recommendService.getUserProfile(userId));
        // 这里可以添加类型偏好等其他信息
        return profile;
    }

    @GetMapping("/cluster")
    public Map<String, Object> getUserCluster(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Map<String, Object> clusterInfo = new java.util.HashMap<>();
        clusterInfo.put("clusterId", recommendService.getUserCluster(userId));
        clusterInfo.put("clusterSize", 100); // 模拟数据
        clusterInfo.put("centerFeatures", "Action, Comedy, Drama"); // 模拟数据
        return clusterInfo;
    }

    @PostMapping("/fill-matrix")
    public com.movie.recommend.dto.SVRFillResultDTO fillRatingMatrix() {
        return recommendService.fillRatingMatrix();
    }
    
    @GetMapping("/performance")
    public Map<String, Object> getAlgorithmPerformance() {
        return recommendService.getAlgorithmPerformance();
    }
}
