package com.movie.collection.service;

import com.movie.collection.entity.Tag;
import com.movie.movie.dto.MovieVO;

import java.util.List;

public interface MovieTagService {
    
    // 为电影添加标签
    void addTagToMovie(Long movieId, Long tagId);
    
    // 为电影添加多个标签
    void addTagsToMovie(Long movieId, List<Long> tagIds);
    
    // 移除电影的标签
    void removeTagFromMovie(Long movieId, Long tagId);
    
    // 获取电影的所有标签
    List<Tag> getTagsByMovieId(Long movieId);
    
    // 获取标签的所有电影ID
    List<Long> getMovieIdsByTagId(Long tagId);
    
    // 根据标签搜索电影
    List<MovieVO> searchMoviesByTag(Long tagId);
    
    // 根据多个标签搜索电影（AND模式）
    List<MovieVO> searchMoviesByTagsAnd(List<Long> tagIds);
    
    // 根据多个标签搜索电影（OR模式）
    List<MovieVO> searchMoviesByTagsOr(List<Long> tagIds);
    
    // 检查电影是否有标签
    boolean hasTag(Long movieId, Long tagId);
    
    // 获取电影的标签数量
    long getMovieTagCount(Long movieId);
}
