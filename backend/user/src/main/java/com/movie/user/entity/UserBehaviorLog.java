package com.movie.user.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_behavior_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_behavior_type", columnList = "behaviorType"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class UserBehaviorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String behaviorType; // CLICK, VIEW, RATING, COLLECTION, SEARCH, NOTE

    @Column(nullable = false)
    private Long targetId;

    @Column(length = 500)
    private String targetInfo; // 额外信息，如搜索关键词

    @Column(length = 50)
    private String sessionId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
