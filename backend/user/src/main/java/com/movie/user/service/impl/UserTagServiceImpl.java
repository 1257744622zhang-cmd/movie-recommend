package com.movie.user.service.impl;

import com.movie.user.entity.UserTag;
import com.movie.user.repository.UserTagRepository;
import com.movie.user.service.UserTagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTagServiceImpl implements UserTagService {
    private final UserTagRepository userTagRepository;

    public UserTagServiceImpl(UserTagRepository userTagRepository) {
        this.userTagRepository = userTagRepository;
    }

    @Override
    public void addOrUpdateTag(Long userId, String tagName, Double weight) {
        UserTag existingTag = userTagRepository.findByUserIdAndTagName(userId, tagName);
        if (existingTag != null) {
            existingTag.setWeight(existingTag.getWeight() + weight);
            userTagRepository.save(existingTag);
        } else {
            UserTag newTag = new UserTag();
            newTag.setUserId(userId);
            newTag.setTagName(tagName);
            newTag.setWeight(weight);
            userTagRepository.save(newTag);
        }
    }

    @Override
    public List<UserTag> getTagsByUserId(Long userId) {
        return userTagRepository.findByUserId(userId);
    }

    @Override
    public void deleteTag(Long userId, String tagName) {
        UserTag tag = userTagRepository.findByUserIdAndTagName(userId, tagName);
        if (tag != null) {
            userTagRepository.delete(tag);
        }
    }
}
