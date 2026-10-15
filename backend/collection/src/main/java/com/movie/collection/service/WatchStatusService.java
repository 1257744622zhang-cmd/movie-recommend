package com.movie.collection.service;

import com.movie.collection.entity.WatchStatus;

import java.util.List;

public interface WatchStatusService {
    
    // 设置观看状态
    WatchStatus setWatchStatus(Long userId, Long movieId, String status);
    
    // 获取观看状态
    String getWatchStatus(Long userId, Long movieId);
    
    // 清除观看状态
    void removeWatchStatus(Long userId, Long movieId);
    
    // 获取用户所有观看状态
    List<WatchStatus> getUserWatchStatuses(Long userId);
    
    // 检查是否有观看状态
    boolean hasWatchStatus(Long userId, Long movieId);
}
