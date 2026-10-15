package com.movie.collection.repository;

import com.movie.collection.entity.MovieTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieTagRepository extends JpaRepository<MovieTag, Long> {
    
    // 获取电影的所有标签
    List<MovieTag> findByMovieId(Long movieId);
    
    // 获取标签的所有电影
    List<MovieTag> findByTagId(Long tagId);
    
    // 删除电影的指定标签
    void deleteByMovieIdAndTagId(Long movieId, Long tagId);
    
    // 删除电影的所有标签
    void deleteByMovieId(Long movieId);
    
    // 删除标签的所有关联
    void deleteByTagId(Long tagId);
    
    // 检查关联是否存在
    boolean existsByMovieIdAndTagId(Long movieId, Long tagId);
    
    // 统计标签使用次数
    long countByTagId(Long tagId);
    
    // 统计电影的标签数量
    long countByMovieId(Long movieId);
}
