package com.movie.controller;

import com.movie.entity.Collection;
import com.movie.service.CollectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collections")
public class CollectionController {
    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public List<Collection> list(@RequestHeader("X-User-Id") Long userId) {
        return collectionService.getByUserId(userId);
    }

    @GetMapping("/folder/{folderName}")
    public List<Collection> listByFolder(@RequestHeader("X-User-Id") Long userId, @PathVariable String folderName) {
        return collectionService.getByUserIdAndCollectionFolder(userId, folderName);
    }

    @GetMapping("/public/{userId}")
    public Map<String, List<Collection>> listPublic(@PathVariable Long userId) {
        return collectionService.getPublicCollectionsByUserId(userId);
    }

    @PostMapping
    public Collection add(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        Long movieId = Long.valueOf(body.get("movieId").toString());
        String collectionFolder = body.containsKey("collectionFolder") ? body.get("collectionFolder").toString() : "默认收藏夹";
        String status = body.containsKey("status") ? body.get("status").toString() : "WANT";
        Boolean isPublic = body.containsKey("isPublic") ? (Boolean) body.get("isPublic") : true;
        return collectionService.addOrUpdate(userId, movieId, collectionFolder, status, isPublic);
    }

    @PostMapping("/folder")
    public void createFolder(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        String collectionFolder = body.get("collectionFolder").toString();
        collectionService.createCollectionFolder(userId, collectionFolder);
    }

    @DeleteMapping
    public void remove(@RequestHeader("X-User-Id") Long userId, @RequestParam Long movieId, @RequestParam(required = false, defaultValue = "默认收藏夹") String collectionFolder) {
        collectionService.remove(userId, movieId, collectionFolder);
    }
    
    @DeleteMapping("/{id}")
    public void removeById(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        // 根据收藏ID删除收藏
        collectionService.removeById(id);
    }

    @GetMapping("/folders")
    public List<String> listFolders(@RequestHeader("X-User-Id") Long userId) {
        return collectionService.getCollectionFoldersByUserId(userId);
    }
}
