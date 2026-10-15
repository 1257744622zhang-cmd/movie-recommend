package com.movie.api.listener;

import com.movie.core.event.UserBehaviorEvent;
import com.movie.user.service.UserBehaviorLogService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserBehaviorEventListener {

    private final UserBehaviorLogService userBehaviorLogService;

    public UserBehaviorEventListener(UserBehaviorLogService userBehaviorLogService) {
        this.userBehaviorLogService = userBehaviorLogService;
    }

    @EventListener
    public void handleUserBehaviorEvent(UserBehaviorEvent event) {
        userBehaviorLogService.logBehavior(
            event.getUserId(),
            event.getBehaviorType(),
            event.getTargetId(),
            event.getTargetInfo()
        );
    }
}
