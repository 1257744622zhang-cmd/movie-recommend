package com.movie.collection.service;

import com.movie.collection.entity.Collection;
import com.movie.movie.dto.MovieVO;
import java.util.List;

public interface CollectionService {
    void addToCollection(Long userId, Long movieId, String status, String collectionName, String permission);
    void removeFromCollection(Long userId, Long movieId);
    void updateStatus(Long userId, Long movieId, String status);
    List<MovieVO> getUserCollections(Long userId);
    List<MovieVO> getUserCollectionByStatus(Long userId, String status);
    List<MovieVO> getUserCollectionByCollectionName(Long userId, String collectionName);
    boolean isMovieCollected(Long userId, Long movieId);
    String getCollectionStatus(Long userId, Long movieId); // 获取收藏状态
}
