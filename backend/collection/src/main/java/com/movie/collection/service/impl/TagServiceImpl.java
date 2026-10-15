package com.movie.collection.service.impl;

import com.movie.collection.entity.MovieTag;
import com.movie.collection.entity.Tag;
import com.movie.collection.repository.MovieTagRepository;
import com.movie.collection.repository.TagRepository;
import com.movie.collection.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final MovieTagRepository movieTagRepository;

    public TagServiceImpl(TagRepository tagRepository, MovieTagRepository movieTagRepository) {
        this.tagRepository = tagRepository;
        this.movieTagRepository = movieTagRepository;
    }

    @Override
    @Transactional
    public Tag createTag(String name, String color, String description, Long userId) {
        // 验证标签名称
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("标签名称不能为空");
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new RuntimeException("标签名称长度需在2-50个字符之间");
        }
        
        // 检查标签是否已存在
        if (tagRepository.existsByName(name.trim())) {
            throw new RuntimeException("标签名称已存在");
        }
        
        // 验证颜色格式
        if (color != null && !color.matches("^#[0-9A-Fa-f]{6}$")) {
            color = "#409EFF"; // 默认颜色
        }
        
        Tag tag = new Tag();
        tag.setName(name.trim());
        tag.setColor(color != null ? color : "#409EFF");
        tag.setDescription(description != null ? description.trim() : null);
        tag.setCreatedBy(userId);
        tag.setUsageCount(0);
        
        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public Tag updateTag(Long tagId, String name, String color, String description) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        // 如果修改了名称，检查是否重复
        if (name != null && !name.trim().isEmpty() && !name.trim().equals(tag.getName())) {
            if (tagRepository.existsByName(name.trim())) {
                throw new RuntimeException("标签名称已存在");
            }
            tag.setName(name.trim());
        }
        
        // 更新颜色
        if (color != null && color.matches("^#[0-9A-Fa-f]{6}$")) {
            tag.setColor(color);
        }
        
        // 更新描述
        if (description != null) {
            tag.setDescription(description.trim());
        }
        
        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        tagRepository.delete(tag);
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAllByOrderByUsageCountDesc();
    }

    @Override
    public Tag getTagById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
    }

    @Override
    public List<Tag> searchTags(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllTags();
        }
        return tagRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public List<Tag> getHotTags(int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        List<Tag> tags = tagRepository.findTop10ByOrderByUsageCountDesc();
        return tags.size() > limit ? tags.subList(0, limit) : tags;
    }

    @Override
    public long getTagUsageCount(Long tagId) {
        return tagRepository.findById(tagId)
                .map(Tag::getUsageCount)
                .orElse(0);
    }

    @Override
    @Transactional
    public void incrementUsageCount(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        tag.setUsageCount(tag.getUsageCount() + 1);
        tagRepository.save(tag);
    }

    @Override
    @Transactional
    public void decrementUsageCount(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        int newCount = tag.getUsageCount() - 1;
        tag.setUsageCount(Math.max(0, newCount));
        tagRepository.save(tag);
    }

    @Override
    @Transactional
    public void batchAddTagsToMovie(Long movieId, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        
        for (String tagName : tags) {
            if (tagName == null || tagName.trim().isEmpty()) {
                continue;
            }
            
            // 查找或创建标签
            Tag tag = tagRepository.findByName(tagName.trim())
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName.trim());
                        newTag.setColor("#409EFF");
                        newTag.setUsageCount(0);
                        return tagRepository.save(newTag);
                    });
            
            // 检查是否已存在关联
            if (!movieTagRepository.existsByMovieIdAndTagId(movieId, tag.getId())) {
                MovieTag movieTag = new MovieTag();
                movieTag.setMovieId(movieId);
                movieTag.setTagId(tag.getId());
                movieTagRepository.save(movieTag);
                
                // 增加标签使用次数
                tag.setUsageCount(tag.getUsageCount() + 1);
                tagRepository.save(tag);
            }
        }
    }
}
