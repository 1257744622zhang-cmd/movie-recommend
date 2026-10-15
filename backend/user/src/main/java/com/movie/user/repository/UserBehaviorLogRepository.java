package com.movie.user.repository;

import com.movie.user.entity.UserBehaviorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserBehaviorLogRepository extends JpaRepository<UserBehaviorLog, Long> {
    List<UserBehaviorLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserBehaviorLog> findByUserIdAndBehaviorTypeOrderByCreatedAtDesc(Long userId, String behaviorType);

    long countByUserIdAndBehaviorTypeAndCreatedAtAfter(Long userId, String behaviorType, LocalDateTime after);

    List<UserBehaviorLog> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime after);

    Page<UserBehaviorLog> findByBehaviorTypeOrderByCreatedAtDesc(String behaviorType, Pageable pageable);
}
