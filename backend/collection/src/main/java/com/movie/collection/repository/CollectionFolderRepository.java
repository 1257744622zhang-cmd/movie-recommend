package com.movie.collection.repository;

import com.movie.collection.entity.CollectionFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionFolderRepository extends JpaRepository<CollectionFolder, Long> {
    
    // 获取用户的所有收藏夹
    List<CollectionFolder> findByUserIdOrderByIsDefaultDescCreateTimeDesc(Long userId);
    
    // 根据用户ID和名称查找收藏夹
    Optional<CollectionFolder> findByUserIdAndName(Long userId, String name);
    
    // 检查用户是否已有该名称的收藏夹
    boolean existsByUserIdAndName(Long userId, String name);
    
    // 获取用户的默认收藏夹
    Optional<CollectionFolder> findByUserIdAndIsDefaultTrue(Long userId);
    
    // 统计用户的收藏夹数量
    long countByUserId(Long userId);
    
    // 获取所有公开收藏夹
    List<CollectionFolder> findByPermissionOrderByCreateTimeDesc(String permission);
    
    // 获取公开收藏夹（排除指定用户自己的）
    List<CollectionFolder> findByPermissionAndUserIdNotOrderByCreateTimeDesc(String permission, Long userId);
}
