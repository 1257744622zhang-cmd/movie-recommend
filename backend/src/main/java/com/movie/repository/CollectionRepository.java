package com.movie.repository;

import com.movie.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    Optional<Collection> findByUserIdAndMovieId(Long userId, Long movieId);
    Optional<Collection> findByUserIdAndMovieIdAndCollectionFolder(Long userId, Long movieId, String collectionFolder);
    List<Collection> findByUserId(Long userId);
    List<Collection> findByUserIdAndIsPublic(Long userId, Boolean isPublic);
    List<Collection> findByUserIdAndCollectionFolder(Long userId, String collectionFolder);
    List<Collection> findByUserIdAndCollectionFolderAndIsPublic(Long userId, String collectionFolder, Boolean isPublic);
    List<String> findDistinctCollectionFolderByUserId(Long userId);
}
