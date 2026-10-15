package com.movie.api.controller;

import com.movie.collection.entity.Tag;
import com.movie.collection.service.TagService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tag")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/list")
    public List<Tag> getAllTags() {
        return tagService.getAllTags();
    }

    @GetMapping("/hot")
    public List<Tag> getHotTags(@RequestParam(defaultValue = "10") int limit) {
        return tagService.getHotTags(limit);
    }

    @GetMapping("/search")
    public List<Tag> searchTags(@RequestParam String keyword) {
        return tagService.searchTags(keyword);
    }

    @GetMapping("/{id}")
    public Tag getTagById(@PathVariable Long id) {
        return tagService.getTagById(id);
    }

    @PostMapping("/create")
    public Tag createTag(Authentication authentication,
                        @RequestBody Map<String, String> body) {
        Long userId = (Long) authentication.getPrincipal();
        String name = body.get("name");
        String color = body.get("color");
        String description = body.get("description");
        return tagService.createTag(name, color, description, userId);
    }

    @PutMapping("/{id}")
    public Tag updateTag(@PathVariable Long id,
                        @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String color = body.get("color");
        String description = body.get("description");
        return tagService.updateTag(id, name, color, description);
    }

    @DeleteMapping("/{id}")
    public void deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
    }

    @GetMapping("/{id}/usage")
    public Map<String, Long> getTagUsageCount(@PathVariable Long id) {
        long count = tagService.getTagUsageCount(id);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return result;
    }
}
