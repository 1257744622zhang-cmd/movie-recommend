package com.movie.recommend.service;

import com.movie.movie.dto.MovieVO;
import com.movie.recommend.dto.SVRFillResultDTO;
import java.util.List;
import java.util.Map;

public interface RecommendService {
    List<MovieVO> getRecommendations(Long userId);
    List<MovieVO> getNewUserRecommendations();
    List<MovieVO> refreshRecommendations(Long userId);
    List<MovieVO> getCFRecommendations(Long userId);
    List<MovieVO> getAutoencoderRecommendations(Long userId);
    List<MovieVO> getHybridRecommendations(Long userId);
    Map<String, Double> getUserProfile(Long userId);
    int getUserCluster(Long userId);
    SVRFillResultDTO fillRatingMatrix();
    Map<String, Object> getAlgorithmPerformance();
}
