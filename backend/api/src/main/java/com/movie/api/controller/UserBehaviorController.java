package com.movie.api.controller;

import com.movie.user.service.UserBehaviorLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/behavior")
public class UserBehaviorController {

    private final UserBehaviorLogService userBehaviorLogService;

    public UserBehaviorController(UserBehaviorLogService userBehaviorLogService) {
        this.userBehaviorLogService = userBehaviorLogService;
    }

    @PostMapping("/log")
    public ResponseEntity<?> logBehavior(Authentication authentication,
                                        @RequestParam String behaviorType,
                                        @RequestParam Long targetId,
                                        @RequestParam(required = false) String targetInfo) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            userBehaviorLogService.logBehavior(userId, behaviorType, targetId, targetInfo);
            return ResponseEntity.ok(createSuccessResponse("记录成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getUserLogs(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(createSuccessResponse("获取成功",
                userBehaviorLogService.getUserLogs(userId)));
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
