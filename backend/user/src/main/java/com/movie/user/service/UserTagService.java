package com.movie.user.service;

import com.movie.user.entity.UserTag;
import java.util.List;

public interface UserTagService {
    void addOrUpdateTag(Long userId, String tagName, Double weight);
    List<UserTag> getTagsByUserId(Long userId);
    void deleteTag(Long userId, String tagName);
}
