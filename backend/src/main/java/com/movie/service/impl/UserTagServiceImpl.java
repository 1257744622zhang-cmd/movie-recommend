package com.movie.service.impl;

import com.movie.entity.Movie;
import com.movie.entity.UserTag;
import com.movie.repository.MovieRepository;
import com.movie.repository.UserTagRepository;
import com.movie.service.UserTagService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserTagServiceImpl implements UserTagService {
    private final UserTagRepository userTagRepository;
    private final MovieRepository movieRepository;

    public UserTagServiceImpl(UserTagRepository userTagRepository, MovieRepository movieRepository) {
        this.userTagRepository = userTagRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    public void addOrUpdateTag(Long userId, String tagName, Double weight) {
        validateTagName(tagName);
        validateWeight(weight);
        Optional<UserTag> existingTag = userTagRepository.findByUserIdAndTagName(userId, tagName);
        if (existingTag.isPresent()) {
            UserTag tag = existingTag.get();
            tag.setWeight(weight);
            userTagRepository.save(tag);
        } else {
            UserTag newTag = new UserTag();
            newTag.setUserId(userId);
            newTag.setTagName(tagName);
            newTag.setWeight(weight);
            userTagRepository.save(newTag);
        }
    }

    @Override
    public void deleteTag(Long userId, String tagName) {
        Optional<UserTag> tag = userTagRepository.findByUserIdAndTagName(userId, tagName);
        tag.ifPresent(userTagRepository::delete);
    }

    @Override
    public void adjustTagWeight(Long userId, String tagName, Double weight) {
        validateWeight(weight);
        Optional<UserTag> tag = userTagRepository.findByUserIdAndTagName(userId, tagName);
        if (tag.isPresent()) {
            UserTag userTag = tag.get();
            userTag.setWeight(weight);
            userTagRepository.save(userTag);
        }
    }

    @Override
    public void updateTagWeightByUserAction(Long userId, Long movieId, String actionType) {
        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        if (movieOpt.isPresent()) {
            Movie movie = movieOpt.get();
            String genre = movie.getGenre();
            if (genre != null && !genre.isEmpty()) {
                double weightIncrement = getWeightIncrementByAction(actionType);
                Optional<UserTag> existingTag = userTagRepository.findByUserIdAndTagName(userId, genre);
                if (existingTag.isPresent()) {
                    UserTag tag = existingTag.get();
                    double newWeight = Math.min(tag.getWeight() + weightIncrement, 10.0);
                    tag.setWeight(Math.round(newWeight * 10) / 10.0);
                    userTagRepository.save(tag);
                } else {
                    UserTag newTag = new UserTag();
                    newTag.setUserId(userId);
                    newTag.setTagName(genre);
                    newTag.setWeight(weightIncrement);
                    userTagRepository.save(newTag);
                }
            }
        }
    }

    @Override
    public List<UserTag> getUserTags(Long userId) {
        return userTagRepository.findByUserId(userId)
                .stream()
                .sorted((t1, t2) -> Double.compare(t2.getWeight(), t1.getWeight()))
                .collect(Collectors.toList());
    }

    private void validateTagName(String tagName) {
        if (tagName == null || tagName.length() < 1 || tagName.length() > 50) {
            throw new IllegalArgumentException("标签名称长度必须在1-50字符之间");
        }
    }

    private void validateWeight(Double weight) {
        if (weight == null || weight < 0.1 || weight > 10.0) {
            throw new IllegalArgumentException("权重必须在0.1-10.0之间");
        }
    }

    private double getWeightIncrementByAction(String actionType) {
        switch (actionType) {
            case "RATE":
                return 5.0;
            case "COLLECT":
                return 3.0;
            case "CLICK":
                return 1.0;
            default:
                return 0.0;
        }
    }
}
