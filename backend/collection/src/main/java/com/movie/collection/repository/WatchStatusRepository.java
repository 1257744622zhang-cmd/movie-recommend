package com.movie.collection.repository;

import com.movie.collection.entity.WatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchStatusRepository extends JpaRepository<WatchStatus, Long> {
    
    // 获取用户的所有观看状态
    List<WatchStatus> findByUserId(Long userId);
    
    // 根据用户ID和电影ID查找观看状态
    Optional<WatchStatus> findByUserIdAndMovieId(Long userId, Long movieId);
    
    // 删除观看状态
    void deleteByUserIdAndMovieId(Long userId, Long movieId);
    
    // 检查是否存在
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);
}
