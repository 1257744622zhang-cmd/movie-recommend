package com.movie.service;

import com.movie.entity.UserTag;

import java.util.List;

public interface UserTagService {
    void addOrUpdateTag(Long userId, String tagName, Double weight);
    void deleteTag(Long userId, String tagName);
    void adjustTagWeight(Long userId, String tagName, Double weight);
    void updateTagWeightByUserAction(Long userId, Long movieId, String actionType);
    List<UserTag> getUserTags(Long userId);
}
