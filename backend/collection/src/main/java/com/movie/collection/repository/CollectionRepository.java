package com.movie.collection.repository;

import com.movie.collection.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByUserId(Long userId);
    List<Collection> findByUserIdAndCollectionName(Long userId, String collectionName);
    List<Collection> findByUserIdAndStatus(Long userId, String status);
    Collection findByUserIdAndMovieId(Long userId, Long movieId);
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    // 根据folderId查询收藏记录
    List<Collection> findByUserIdAndFolderId(Long userId, Long folderId);

    // 根据folderId统计数量
    long countByFolderId(Long folderId);
}
