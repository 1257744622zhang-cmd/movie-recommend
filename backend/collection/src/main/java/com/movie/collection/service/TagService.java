package com.movie.collection.service;

import com.movie.collection.entity.Tag;

import java.util.List;

public interface TagService {
    
    // 创建标签
    Tag createTag(String name, String color, String description, Long userId);
    
    // 更新标签
    Tag updateTag(Long tagId, String name, String color, String description);
    
    // 删除标签
    void deleteTag(Long tagId);
    
    // 获取所有标签
    List<Tag> getAllTags();
    
    // 根据ID获取标签
    Tag getTagById(Long tagId);
    
    // 搜索标签
    List<Tag> searchTags(String keyword);
    
    // 获取热门标签
    List<Tag> getHotTags(int limit);
    
    // 获取标签使用次数
    long getTagUsageCount(Long tagId);
    
    // 增加标签使用次数
    void incrementUsageCount(Long tagId);
    
    // 减少标签使用次数
    void decrementUsageCount(Long tagId);
    
    // 批量为电影添加标签
    void batchAddTagsToMovie(Long movieId, List<String> tags);
}
