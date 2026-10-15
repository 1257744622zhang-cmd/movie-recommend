package com.movie.collection.repository;

import com.movie.collection.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    // 根据名称查找标签
    Optional<Tag> findByName(String name);
    
    // 检查标签是否存在
    boolean existsByName(String name);
    
    // 按使用次数排序获取标签
    List<Tag> findAllByOrderByUsageCountDesc();
    
    // 搜索标签（模糊查询）
    List<Tag> findByNameContainingIgnoreCase(String keyword);
    
    // 获取热门标签（使用次数最多的N个）
    List<Tag> findTop10ByOrderByUsageCountDesc();
    
    // 根据使用次数范围查询
    List<Tag> findByUsageCountGreaterThanEqual(int minUsage);
}
