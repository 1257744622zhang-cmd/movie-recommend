package com.movie.repository;

import com.movie.entity.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTagRepository extends JpaRepository<UserTag, Long> {
    List<UserTag> findByUserId(Long userId);
    Optional<UserTag> findByUserIdAndTagName(Long userId, String tagName);
}
