package com.movie.service.impl;
import com.movie.entity.RecommendFeedback;
import com.movie.repository.RecommendFeedbackRepository;
import com.movie.service.CollectionService;
import com.movie.service.RecommendFeedbackService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendFeedbackServiceImpl implements RecommendFeedbackService {
    private final RecommendFeedbackRepository feedbackRepository;
    private final CollectionService collectionService;

    public RecommendFeedbackServiceImpl(RecommendFeedbackRepository feedbackRepository, CollectionService collectionService) {
        this.feedbackRepository = feedbackRepository;
        this.collectionService = collectionService;
    }

    @Override
    public RecommendFeedback addDislike(Long userId, Long movieId) {
        // 检查是否已经标记为不喜欢
        if (feedbackRepository.existsByUserIdAndMovieIdAndFeedbackType(userId, movieId, "dislike")) {
            return null; // 已经标记过，无需重复添加
        }
        
        // 如果电影已经被收藏，取消收藏状态
        boolean isCollected = collectionService.getByUserIdAndCollectionFolder(userId, "默认收藏夹").stream()
                .anyMatch(c -> c.getMovieId().equals(movieId));
        if (isCollected) {
            collectionService.remove(userId, movieId, "默认收藏夹");
        }
        
        RecommendFeedback feedback = new RecommendFeedback(userId, movieId, "dislike");
        return feedbackRepository.save(feedback);
    }

    @Override
    public void removeDislike(Long userId, Long movieId) {
        List<RecommendFeedback> feedbacks = feedbackRepository.findByUserIdAndFeedbackType(userId, "dislike");
        feedbacks.stream()
                .filter(f -> f.getMovieId().equals(movieId))
                .findAny()
                .ifPresent(feedbackRepository::delete);
    }

    @Override
    public List<RecommendFeedback> getDislikedMovies(Long userId) {
        return feedbackRepository.findByUserIdAndFeedbackType(userId, "dislike");
    }

    @Override
    public boolean isDisliked(Long userId, Long movieId) {
        return feedbackRepository.existsByUserIdAndMovieIdAndFeedbackType(userId, movieId, "dislike");
    }

    @Override
    public List<Long> getDislikedMovieIds(Long userId) {
        return feedbackRepository.findByUserIdAndFeedbackType(userId, "dislike").stream()
                .map(RecommendFeedback::getMovieId)
                .collect(Collectors.toList());
    }
}
