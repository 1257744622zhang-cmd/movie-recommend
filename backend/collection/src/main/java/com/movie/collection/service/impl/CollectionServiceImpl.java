package com.movie.collection.service.impl;

import com.movie.collection.entity.Collection;
import com.movie.collection.repository.CollectionRepository;
import com.movie.collection.service.CollectionService;
import com.movie.core.event.UserBehaviorEvent;
import com.movie.movie.dto.MovieVO;
import com.movie.movie.service.MovieService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;
    private final MovieService movieService;
    private final ApplicationEventPublisher eventPublisher;

    public CollectionServiceImpl(CollectionRepository collectionRepository, MovieService movieService, ApplicationEventPublisher eventPublisher) {
        this.collectionRepository = collectionRepository;
        this.movieService = movieService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void addToCollection(Long userId, Long movieId, String status, String collectionName, String permission) {
        Collection existingCollection = collectionRepository.findByUserIdAndMovieId(userId, movieId);
        if (existingCollection != null) {
            // 更新现有收藏
            existingCollection.setStatus(status != null ? status : existingCollection.getStatus());
            existingCollection.setCollectionName(collectionName != null ? collectionName : existingCollection.getCollectionName());
            existingCollection.setPermission(permission != null ? permission : existingCollection.getPermission());
            collectionRepository.save(existingCollection);
        } else {
            // 创建新收藏
            Collection collection = new Collection();
            collection.setUserId(userId);
            collection.setMovieId(movieId);
            collection.setStatus(status != null ? status : "COLLECTED");
            collection.setCollectionName(collectionName != null ? collectionName : "默认收藏夹");
            collection.setPermission(permission != null ? permission : "PRIVATE");
            collectionRepository.save(collection);
        }
        // 发布收藏行为事件
        eventPublisher.publishEvent(new UserBehaviorEvent(this, userId, "COLLECT", movieId, null));
    }

    @Override
    public void removeFromCollection(Long userId, Long movieId) {
        Collection collection = collectionRepository.findByUserIdAndMovieId(userId, movieId);
        if (collection != null) {
            collectionRepository.delete(collection);
        }
    }

    @Override
    public void updateStatus(Long userId, Long movieId, String status) {
        Collection collection = collectionRepository.findByUserIdAndMovieId(userId, movieId);
        if (collection != null) {
            collection.setStatus(status);
            collectionRepository.save(collection);
        }
    }

    @Override
    public List<MovieVO> getUserCollections(Long userId) {
        List<Collection> collections = collectionRepository.findByUserId(userId);
        return collections.stream()
                .map(collection -> {
                    MovieVO movie = movieService.getMovieById(collection.getMovieId());
                    if (movie != null) {
                        movie.setStatus(collection.getStatus());
                    }
                    return movie;
                })
                .filter(movie -> movie != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieVO> getUserCollectionByStatus(Long userId, String status) {
        List<Collection> collections = collectionRepository.findByUserIdAndStatus(userId, status);
        return collections.stream()
                .map(collection -> {
                    MovieVO movie = movieService.getMovieById(collection.getMovieId());
                    if (movie != null) {
                        movie.setStatus(collection.getStatus());
                    }
                    return movie;
                })
                .filter(movie -> movie != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieVO> getUserCollectionByCollectionName(Long userId, String collectionName) {
        List<Collection> collections = collectionRepository.findByUserIdAndCollectionName(userId, collectionName);
        return collections.stream()
                .map(collection -> movieService.getMovieById(collection.getMovieId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isMovieCollected(Long userId, Long movieId) {
        return collectionRepository.existsByUserIdAndMovieId(userId, movieId);
    }

    @Override
    public String getCollectionStatus(Long userId, Long movieId) {
        Collection collection = collectionRepository.findByUserIdAndMovieId(userId, movieId);
        return collection != null ? collection.getStatus() : null;
    }
}
