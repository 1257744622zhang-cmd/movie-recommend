package com.movie.user.repository;

import com.movie.user.entity.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserTagRepository extends JpaRepository<UserTag, Long> {
    List<UserTag> findByUserId(Long userId);
    UserTag findByUserIdAndTagName(Long userId, String tagName);
}
