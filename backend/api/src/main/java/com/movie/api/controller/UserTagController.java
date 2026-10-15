package com.movie.api.controller;

import com.movie.user.entity.UserTag;
import com.movie.user.service.UserTagService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-tag")
public class UserTagController {
    private final UserTagService userTagService;

    public UserTagController(UserTagService userTagService) {
        this.userTagService = userTagService;
    }

    @PostMapping("/add")
    public void addTag(Authentication authentication, 
                      @RequestParam String tagName, 
                      @RequestParam(required = false) Double weight) {
        Long userId = (Long) authentication.getPrincipal();
        userTagService.addOrUpdateTag(userId, tagName, weight != null ? weight : 1.0);
    }

    @GetMapping("/user")
    public List<UserTag> getUserTags(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userTagService.getTagsByUserId(userId);
    }

    @DeleteMapping("/delete")
    public void deleteTag(Authentication authentication, @RequestParam String tagName) {
        Long userId = (Long) authentication.getPrincipal();
        userTagService.deleteTag(userId, tagName);
    }
}
