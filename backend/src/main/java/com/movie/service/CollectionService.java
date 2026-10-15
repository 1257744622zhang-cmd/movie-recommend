package com.movie.service;

import com.movie.entity.Collection;
import com.movie.repository.CollectionRepository;
import com.movie.service.RecommendFeedbackService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final RecommendFeedbackService feedbackService;

    public CollectionService(CollectionRepository collectionRepository, @org.springframework.context.annotation.Lazy RecommendFeedbackService feedbackService) {
        this.collectionRepository = collectionRepository;
        this.feedbackService = feedbackService;
    }

    public Collection addOrUpdate(Long userId, Long movieId, String collectionFolder, String status, Boolean isPublic) {
        Optional<Collection> opt = collectionRepository.findByUserIdAndMovieIdAndCollectionFolder(userId, movieId, collectionFolder);
        Collection c = opt.orElseGet(() -> {
            Collection nc = new Collection();
            nc.setUserId(userId);
            nc.setMovieId(movieId);
            nc.setCollectionFolder(collectionFolder);
            return nc;
        });
        if (status != null) c.setStatus(status);
        if (isPublic != null) c.setIsPublic(isPublic);
        
        // 如果电影已经被标记为不喜欢，取消不喜欢状态
        if (feedbackService.isDisliked(userId, movieId)) {
            feedbackService.removeDislike(userId, movieId);
        }
        
        return collectionRepository.save(c);
    }

    public void remove(Long userId, Long movieId, String collectionFolder) {
        collectionRepository.findByUserIdAndMovieIdAndCollectionFolder(userId, movieId, collectionFolder).ifPresent(collectionRepository::delete);
    }
    
    public void removeById(Long id) {
        collectionRepository.findById(id).ifPresent(collectionRepository::delete);
    }

    public List<Collection> getByUserId(Long userId) {
        return collectionRepository.findByUserId(userId);
    }

    public List<Collection> getByUserIdAndCollectionFolder(Long userId, String collectionFolder) {
        return collectionRepository.findByUserIdAndCollectionFolder(userId, collectionFolder);
    }

    public Map<String, List<Collection>> getPublicCollectionsByUserId(Long userId) {
        List<Collection> publicCollections = collectionRepository.findByUserIdAndIsPublic(userId, true);
        return publicCollections.stream()
                .collect(Collectors.groupingBy(Collection::getCollectionFolder));
    }

    public List<String> getCollectionFoldersByUserId(Long userId) {
        return collectionRepository.findDistinctCollectionFolderByUserId(userId);
    }

    public void createCollectionFolder(Long userId, String collectionFolder) {
        if (collectionFolder == null || collectionFolder.length() < 1 || collectionFolder.length() > 20) {
            throw new IllegalArgumentException("收藏夹名称长度必须在1-20字符之间");
        }
    }
}
