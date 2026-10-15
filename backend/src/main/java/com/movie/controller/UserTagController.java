package com.movie.controller;

import com.movie.entity.UserTag;
import com.movie.service.UserTagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-tags")
public class UserTagController {
    private final UserTagService userTagService;

    public UserTagController(UserTagService userTagService) {
        this.userTagService = userTagService;
    }

    @PostMapping
    public ResponseEntity<Void> addOrUpdateTag(@RequestHeader("X-User-Id") Long userId, @RequestParam String tagName, @RequestParam Double weight) {
        userTagService.addOrUpdateTag(userId, tagName, weight);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTag(@RequestHeader("X-User-Id") Long userId, @RequestParam String tagName) {
        userTagService.deleteTag(userId, tagName);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> adjustTagWeight(@RequestHeader("X-User-Id") Long userId, @RequestParam String tagName, @RequestParam Double weight) {
        userTagService.adjustTagWeight(userId, tagName, weight);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserTag>> getUserTags(@RequestHeader("X-User-Id") Long userId) {
        List<UserTag> tags = userTagService.getUserTags(userId);
        return ResponseEntity.ok(tags);
    }
}
