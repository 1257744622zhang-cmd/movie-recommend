package com.movie.user.service;

import com.movie.user.entity.UserBehaviorLog;
import java.util.List;

public interface UserBehaviorLogService {
    void logBehavior(Long userId, String behaviorType, Long targetId, String targetInfo);

    List<UserBehaviorLog> getUserLogs(Long userId);

    List<UserBehaviorLog> getUserLogsByType(Long userId, String behaviorType);
}
