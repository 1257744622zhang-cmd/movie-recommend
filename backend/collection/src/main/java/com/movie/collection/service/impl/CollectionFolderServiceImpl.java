package com.movie.collection.service.impl;

import com.movie.collection.entity.Collection;
import com.movie.collection.entity.CollectionFolder;
import com.movie.collection.repository.CollectionFolderRepository;
import com.movie.collection.repository.CollectionRepository;
import com.movie.collection.service.CollectionFolderService;
import com.movie.movie.dto.MovieVO;
import com.movie.movie.service.MovieService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CollectionFolderServiceImpl implements CollectionFolderService {

    private final CollectionFolderRepository folderRepository;
    private final CollectionRepository collectionRepository;
    private final MovieService movieService;

    public CollectionFolderServiceImpl(CollectionFolderRepository folderRepository,
                                       CollectionRepository collectionRepository,
                                       MovieService movieService) {
        this.folderRepository = folderRepository;
        this.collectionRepository = collectionRepository;
        this.movieService = movieService;
    }

    @Override
    @Transactional
    public CollectionFolder createFolder(Long userId, String name, String description, String permission) {
        // 验证名称
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("收藏夹名称不能为空");
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new RuntimeException("收藏夹名称长度需在2-50个字符之间");
        }
        
        // 检查名称是否已存在
        if (folderRepository.existsByUserIdAndName(userId, name)) {
            throw new RuntimeException("收藏夹名称已存在");
        }
        
        CollectionFolder folder = new CollectionFolder();
        folder.setUserId(userId);
        folder.setName(name.trim());
        folder.setDescription(description != null ? description.trim() : null);
        folder.setPermission(permission != null ? permission : "PRIVATE");
        folder.setIsDefault(false);
        
        return folderRepository.save(folder);
    }

    @Override
    @Transactional
    public CollectionFolder renameFolder(Long userId, Long folderId, String newName) {
        CollectionFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("收藏夹不存在"));
        
        // 验证权限
        if (!folder.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此收藏夹");
        }
        
        // 默认收藏夹不能重命名
        if (folder.getIsDefault()) {
            throw new RuntimeException("默认收藏夹不能重命名");
        }
        
        // 验证新名称
        if (newName == null || newName.trim().isEmpty()) {
            throw new RuntimeException("收藏夹名称不能为空");
        }
        if (newName.length() < 2 || newName.length() > 50) {
            throw new RuntimeException("收藏夹名称长度需在2-50个字符之间");
        }
        
        // 检查新名称是否已存在（且不是当前收藏夹）
        Optional<CollectionFolder> existing = folderRepository.findByUserIdAndName(userId, newName.trim());
        if (existing.isPresent() && !existing.get().getId().equals(folderId)) {
            throw new RuntimeException("收藏夹名称已存在");
        }
        
        folder.setName(newName.trim());
        return folderRepository.save(folder);
    }

    @Override
    @Transactional
    public void deleteFolder(Long userId, Long folderId, boolean moveToDefault) {
        CollectionFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("收藏夹不存在"));
        
        // 验证权限
        if (!folder.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此收藏夹");
        }
        
        // 默认收藏夹不能删除
        if (folder.getIsDefault()) {
            throw new RuntimeException("默认收藏夹不能删除");
        }
        
        if (moveToDefault) {
            // 将电影移动到默认收藏夹
            CollectionFolder defaultFolder = getOrCreateDefaultFolder(userId);
            List<Collection> collections = collectionRepository.findByUserId(userId);
            for (Collection collection : collections) {
                if (folderId.equals(collection.getFolderId())) {
                    collection.setFolderId(defaultFolder.getId());
                    collectionRepository.save(collection);
                }
            }
        } else {
            // 删除收藏夹中的所有收藏记录
            List<Collection> collections = collectionRepository.findByUserId(userId);
            for (Collection collection : collections) {
                if (folderId.equals(collection.getFolderId())) {
                    collectionRepository.delete(collection);
                }
            }
        }
        
        folderRepository.delete(folder);
    }

    @Override
    public List<CollectionFolder> getUserFolders(Long userId) {
        return folderRepository.findByUserIdOrderByIsDefaultDescCreateTimeDesc(userId);
    }

    @Override
    public CollectionFolder getFolderById(Long folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("收藏夹不存在"));
    }

    @Override
    public List<MovieVO> getFolderMovies(Long userId, Long folderId) {
        // 验证权限
        CollectionFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("收藏夹不存在"));
        if (!folder.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此收藏夹");
        }
        
        List<Collection> collections = collectionRepository.findByUserId(userId);
        return collections.stream()
                .filter(c -> folderId.equals(c.getFolderId()))
                .map(c -> {
                    MovieVO movie = movieService.getMovieById(c.getMovieId());
                    if (movie != null) {
                        movie.setStatus(c.getStatus());
                    }
                    return movie;
                })
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void moveMovieToFolder(Long userId, Long movieId, Long targetFolderId) {
        // 验证目标收藏夹
        CollectionFolder targetFolder = folderRepository.findById(targetFolderId)
                .orElseThrow(() -> new RuntimeException("目标收藏夹不存在"));
        if (!targetFolder.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问目标收藏夹");
        }
        
        // 查找收藏记录
        Collection collection = collectionRepository.findByUserIdAndMovieId(userId, movieId);
        if (collection == null) {
            throw new RuntimeException("电影不在收藏中");
        }
        
        collection.setFolderId(targetFolderId);
        collectionRepository.save(collection);
    }

    @Override
    @Transactional
    public void batchMoveMoviesToFolder(Long userId, List<Long> movieIds, Long targetFolderId) {
        // 验证目标收藏夹
        CollectionFolder targetFolder = folderRepository.findById(targetFolderId)
                .orElseThrow(() -> new RuntimeException("目标收藏夹不存在"));
        if (!targetFolder.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问目标收藏夹");
        }
        
        for (Long movieId : movieIds) {
            Collection collection = collectionRepository.findByUserIdAndMovieId(userId, movieId);
            if (collection != null) {
                collection.setFolderId(targetFolderId);
                collectionRepository.save(collection);
            }
        }
    }

    @Override
    @Transactional
    public CollectionFolder getOrCreateDefaultFolder(Long userId) {
        Optional<CollectionFolder> defaultFolder = folderRepository.findByUserIdAndIsDefaultTrue(userId);
        if (defaultFolder.isPresent()) {
            return defaultFolder.get();
        }
        
        // 创建默认收藏夹
        CollectionFolder folder = new CollectionFolder();
        folder.setUserId(userId);
        folder.setName("默认收藏夹");
        folder.setDescription("系统自动创建的默认收藏夹");
        folder.setPermission("PRIVATE");
        folder.setIsDefault(true);
        return folderRepository.save(folder);
    }

    @Override
    public long getFolderMovieCount(Long folderId) {
        CollectionFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("收藏夹不存在"));
        
        List<Collection> collections = collectionRepository.findByUserId(folder.getUserId());
        return collections.stream()
                .filter(c -> folderId.equals(c.getFolderId()))
                .count();
    }

    @Override
    @Transactional
    public CollectionFolder setFolderPermission(Long userId, Long folderId, String permission) {
        // 验证权限值
        if (!permission.equals("PRIVATE") && !permission.equals("PUBLIC")) {
            throw new RuntimeException("无效的权限值，只能是 PRIVATE 或 PUBLIC");
        }
        
        CollectionFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("收藏夹不存在"));
        
        // 验证权限
        if (!folder.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此收藏夹");
        }
        
        // 默认收藏夹不能设置为公开
        if (folder.getIsDefault() && permission.equals("PUBLIC")) {
            throw new RuntimeException("默认收藏夹不能设置为公开");
        }
        
        folder.setPermission(permission);
        return folderRepository.save(folder);
    }

    @Override
    public List<CollectionFolder> getPublicFolders() {
        return folderRepository.findByPermissionOrderByCreateTimeDesc("PUBLIC");
    }

    @Override
    public boolean hasAccessToFolder(Long userId, Long folderId) {
        CollectionFolder folder = folderRepository.findById(folderId)
                .orElse(null);
        
        if (folder == null) {
            return false;
        }
        
        // 自己的收藏夹总是有权限
        if (folder.getUserId().equals(userId)) {
            return true;
        }
        
        // 公开的收藏夹有权限
        return "PUBLIC".equals(folder.getPermission());
    }
}
