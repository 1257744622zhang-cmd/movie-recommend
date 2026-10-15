package com.movie.api.controller;

import com.movie.collection.entity.CollectionFolder;
import com.movie.collection.service.CollectionFolderService;
import com.movie.movie.dto.MovieVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/folder")
public class CollectionFolderController {

    private final CollectionFolderService folderService;

    public CollectionFolderController(CollectionFolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping("/create")
    public CollectionFolder createFolder(Authentication authentication,
                                        @RequestParam String name,
                                        @RequestParam(required = false) String description,
                                        @RequestParam(required = false, defaultValue = "PRIVATE") String permission) {
        Long userId = (Long) authentication.getPrincipal();
        return folderService.createFolder(userId, name, description, permission);
    }

    @PutMapping("/{id}/rename")
    public CollectionFolder renameFolder(Authentication authentication,
                                        @PathVariable Long id,
                                        @RequestParam String newName) {
        Long userId = (Long) authentication.getPrincipal();
        return folderService.renameFolder(userId, id, newName);
    }

    @DeleteMapping("/{id}")
    public void deleteFolder(Authentication authentication,
                            @PathVariable Long id,
                            @RequestParam(required = false, defaultValue = "true") boolean moveToDefault) {
        Long userId = (Long) authentication.getPrincipal();
        folderService.deleteFolder(userId, id, moveToDefault);
    }

    @GetMapping("/list")
    public List<CollectionFolder> getUserFolders(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return folderService.getUserFolders(userId);
    }

    @GetMapping("/{id}/movies")
    public List<MovieVO> getFolderMovies(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return folderService.getFolderMovies(userId, id);
    }

    @PostMapping("/{id}/move")
    public void moveMovieToFolder(Authentication authentication,
                                 @PathVariable Long id,
                                 @RequestParam Long movieId) {
        Long userId = (Long) authentication.getPrincipal();
        folderService.moveMovieToFolder(userId, movieId, id);
    }

    @PostMapping("/{id}/batch-move")
    public void batchMoveMoviesToFolder(Authentication authentication,
                                       @PathVariable Long id,
                                       @RequestBody List<Long> movieIds) {
        Long userId = (Long) authentication.getPrincipal();
        folderService.batchMoveMoviesToFolder(userId, movieIds, id);
    }

    @GetMapping("/{id}/count")
    public Map<String, Long> getFolderMovieCount(@PathVariable Long id) {
        long count = folderService.getFolderMovieCount(id);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return result;
    }

    @PutMapping("/{id}/permission")
    public CollectionFolder setFolderPermission(Authentication authentication,
                                               @PathVariable Long id,
                                               @RequestParam String permission) {
        Long userId = (Long) authentication.getPrincipal();
        return folderService.setFolderPermission(userId, id, permission);
    }

    @GetMapping("/public")
    public List<CollectionFolder> getPublicFolders(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        // 返回公开的收藏夹（排除用户自己的）
        return folderService.getPublicFolders().stream()
                .filter(f -> !f.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/detail")
    public CollectionFolder getFolderDetail(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        // 检查访问权限
        if (!folderService.hasAccessToFolder(userId, id)) {
            throw new RuntimeException("无权访问此收藏夹");
        }
        return folderService.getFolderById(id);
    }
}
