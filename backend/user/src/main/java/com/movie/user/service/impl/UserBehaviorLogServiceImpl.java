package com.movie.user.service.impl;

import com.movie.user.entity.UserBehaviorLog;
import com.movie.user.repository.UserBehaviorLogRepository;
import com.movie.user.service.UserBehaviorLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBehaviorLogServiceImpl implements UserBehaviorLogService {

    private final UserBehaviorLogRepository userBehaviorLogRepository;

    public UserBehaviorLogServiceImpl(UserBehaviorLogRepository userBehaviorLogRepository) {
        this.userBehaviorLogRepository = userBehaviorLogRepository;
    }

    @Override
    public void logBehavior(Long userId, String behaviorType, Long targetId, String targetInfo) {
        UserBehaviorLog log = new UserBehaviorLog();
        log.setUserId(userId);
        log.setBehaviorType(behaviorType);
        log.setTargetId(targetId);
        log.setTargetInfo(targetInfo);
        userBehaviorLogRepository.save(log);
    }

    @Override
    public List<UserBehaviorLog> getUserLogs(Long userId) {
        return userBehaviorLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<UserBehaviorLog> getUserLogsByType(Long userId, String behaviorType) {
        return userBehaviorLogRepository.findByUserIdAndBehaviorTypeOrderByCreatedAtDesc(userId, behaviorType);
    }
}
