package com.movie.core.event;

import org.springframework.context.ApplicationEvent;

public class UserBehaviorEvent extends ApplicationEvent {
    private final Long userId;
    private final String behaviorType;
    private final Long targetId;
    private final String targetInfo;

    public UserBehaviorEvent(Object source, Long userId, String behaviorType, Long targetId, String targetInfo) {
        super(source);
        this.userId = userId;
        this.behaviorType = behaviorType;
        this.targetId = targetId;
        this.targetInfo = targetInfo;
    }

    public Long getUserId() {
        return userId;
    }

    public String getBehaviorType() {
        return behaviorType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getTargetInfo() {
        return targetInfo;
    }
}
