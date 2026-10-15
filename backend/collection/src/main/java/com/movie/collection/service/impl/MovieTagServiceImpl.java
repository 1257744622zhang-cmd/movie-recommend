package com.movie.collection.service.impl;

import com.movie.collection.entity.MovieTag;
import com.movie.collection.entity.Tag;
import com.movie.collection.repository.MovieTagRepository;
import com.movie.collection.repository.TagRepository;
import com.movie.collection.service.MovieTagService;
import com.movie.collection.service.TagService;
import com.movie.movie.dto.MovieVO;
import com.movie.movie.service.MovieService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieTagServiceImpl implements MovieTagService {

    private final MovieTagRepository movieTagRepository;
    private final TagRepository tagRepository;
    private final TagService tagService;
    private final MovieService movieService;

    public MovieTagServiceImpl(MovieTagRepository movieTagRepository,
                               TagRepository tagRepository,
                               TagService tagService,
                               MovieService movieService) {
        this.movieTagRepository = movieTagRepository;
        this.tagRepository = tagRepository;
        this.tagService = tagService;
        this.movieService = movieService;
    }

    @Override
    @Transactional
    public void addTagToMovie(Long movieId, Long tagId) {
        // 检查标签是否存在
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        // 检查是否已关联
        if (movieTagRepository.existsByMovieIdAndTagId(movieId, tagId)) {
            throw new RuntimeException("该标签已添加到此电影");
        }
        
        // 创建关联
        MovieTag movieTag = new MovieTag();
        movieTag.setMovieId(movieId);
        movieTag.setTagId(tagId);
        movieTagRepository.save(movieTag);
        
        // 增加标签使用次数
        tagService.incrementUsageCount(tagId);
    }

    @Override
    @Transactional
    public void addTagsToMovie(Long movieId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        
        for (Long tagId : tagIds) {
            try {
                addTagToMovie(movieId, tagId);
            } catch (Exception e) {
                // 忽略已存在的标签
                System.out.println("标签 " + tagId + " 已存在或添加失败: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void removeTagFromMovie(Long movieId, Long tagId) {
        // 检查关联是否存在
        if (!movieTagRepository.existsByMovieIdAndTagId(movieId, tagId)) {
            throw new RuntimeException("该电影没有此标签");
        }
        
        // 删除关联
        movieTagRepository.deleteByMovieIdAndTagId(movieId, tagId);
        
        // 减少标签使用次数
        tagService.decrementUsageCount(tagId);
    }

    @Override
    public List<Tag> getTagsByMovieId(Long movieId) {
        List<MovieTag> movieTags = movieTagRepository.findByMovieId(movieId);
        
        List<Tag> tags = new ArrayList<>();
        for (MovieTag movieTag : movieTags) {
            tagRepository.findById(movieTag.getTagId()).ifPresent(tags::add);
        }
        
        return tags;
    }

    @Override
    public List<Long> getMovieIdsByTagId(Long tagId) {
        List<MovieTag> movieTags = movieTagRepository.findByTagId(tagId);
        return movieTags.stream()
                .map(MovieTag::getMovieId)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieVO> searchMoviesByTag(Long tagId) {
        List<Long> movieIds = getMovieIdsByTagId(tagId);
        
        List<MovieVO> movies = new ArrayList<>();
        for (Long movieId : movieIds) {
            MovieVO movie = movieService.getMovieById(movieId);
            if (movie != null) {
                movies.add(movie);
            }
        }
        
        return movies;
    }

    @Override
    public List<MovieVO> searchMoviesByTagsAnd(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取第一个标签的电影列表
        List<Long> movieIds = new ArrayList<>(getMovieIdsByTagId(tagIds.get(0)));
        
        // 逐个过滤，只保留包含所有标签的电影
        for (int i = 1; i < tagIds.size(); i++) {
            List<Long> currentTagMovieIds = getMovieIdsByTagId(tagIds.get(i));
            movieIds.retainAll(currentTagMovieIds);
            
            // 如果已经没有共同电影，提前返回
            if (movieIds.isEmpty()) {
                return Collections.emptyList();
            }
        }
        
        // 获取电影详情
        List<MovieVO> movies = new ArrayList<>();
        for (Long movieId : movieIds) {
            MovieVO movie = movieService.getMovieById(movieId);
            if (movie != null) {
                movies.add(movie);
            }
        }
        
        return movies;
    }

    @Override
    public List<MovieVO> searchMoviesByTagsOr(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 收集所有包含任一标签的电影ID（去重）
        List<Long> movieIds = new ArrayList<>();
        for (Long tagId : tagIds) {
            List<Long> currentTagMovieIds = getMovieIdsByTagId(tagId);
            for (Long movieId : currentTagMovieIds) {
                if (!movieIds.contains(movieId)) {
                    movieIds.add(movieId);
                }
            }
        }
        
        // 获取电影详情
        List<MovieVO> movies = new ArrayList<>();
        for (Long movieId : movieIds) {
            MovieVO movie = movieService.getMovieById(movieId);
            if (movie != null) {
                movies.add(movie);
            }
        }
        
        return movies;
    }

    @Override
    public boolean hasTag(Long movieId, Long tagId) {
        return movieTagRepository.existsByMovieIdAndTagId(movieId, tagId);
    }

    @Override
    public long getMovieTagCount(Long movieId) {
        return movieTagRepository.countByMovieId(movieId);
    }
}
