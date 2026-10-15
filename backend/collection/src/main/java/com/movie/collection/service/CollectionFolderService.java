package com.movie.collection.service;

import com.movie.collection.entity.CollectionFolder;
import com.movie.movie.dto.MovieVO;

import java.util.List;

public interface CollectionFolderService {
    
    // 创建收藏夹
    CollectionFolder createFolder(Long userId, String name, String description, String permission);
    
    // 重命名收藏夹
    CollectionFolder renameFolder(Long userId, Long folderId, String newName);
    
    // 删除收藏夹
    void deleteFolder(Long userId, Long folderId, boolean moveToDefault);
    
    // 获取用户的所有收藏夹
    List<CollectionFolder> getUserFolders(Long userId);
    
    // 获取收藏夹详情
    CollectionFolder getFolderById(Long folderId);
    
    // 获取收藏夹中的电影
    List<MovieVO> getFolderMovies(Long userId, Long folderId);
    
    // 移动电影到收藏夹
    void moveMovieToFolder(Long userId, Long movieId, Long targetFolderId);
    
    // 批量移动电影到收藏夹
    void batchMoveMoviesToFolder(Long userId, List<Long> movieIds, Long targetFolderId);
    
    // 获取或创建默认收藏夹
    CollectionFolder getOrCreateDefaultFolder(Long userId);
    
    // 获取收藏夹中的电影数量
    long getFolderMovieCount(Long folderId);
    
    // 设置收藏夹权限
    CollectionFolder setFolderPermission(Long userId, Long folderId, String permission);
    
    // 获取公开的收藏夹列表
    List<CollectionFolder> getPublicFolders();
    
    // 检查用户是否有权限访问收藏夹
    boolean hasAccessToFolder(Long userId, Long folderId);
}
