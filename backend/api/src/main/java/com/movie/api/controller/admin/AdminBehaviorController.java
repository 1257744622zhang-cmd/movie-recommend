package com.movie.api.controller.admin;

import com.movie.user.entity.UserBehaviorLog;
import com.movie.user.repository.UserBehaviorLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/behavior")
public class AdminBehaviorController {

    private final UserBehaviorLogRepository userBehaviorLogRepository;

    public AdminBehaviorController(UserBehaviorLogRepository userBehaviorLogRepository) {
        this.userBehaviorLogRepository = userBehaviorLogRepository;
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String behaviorType) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserBehaviorLog> logs;
        
        System.out.println("筛选类型: " + behaviorType);
        
        if (behaviorType != null && !behaviorType.isEmpty()) {
            logs = userBehaviorLogRepository.findByBehaviorTypeOrderByCreatedAtDesc(behaviorType, pageRequest);
            System.out.println("按类型筛选，结果数量: " + logs.getTotalElements());
        } else {
            logs = userBehaviorLogRepository.findAll(pageRequest);
            System.out.println("获取全部，结果数量: " + logs.getTotalElements());
        }
        
        return ResponseEntity.ok(createSuccessResponse("获取成功", logs));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getBehaviorStats() {
        List<UserBehaviorLog> allLogs = userBehaviorLogRepository.findAll();
        
        // 统计各类型行为数量
        Map<String, Long> typeCount = allLogs.stream()
                .collect(Collectors.groupingBy(UserBehaviorLog::getBehaviorType, Collectors.counting()));
        
        // 统计活跃用户（有行为的用户）
        long activeUsers = allLogs.stream()
                .map(UserBehaviorLog::getUserId)
                .distinct()
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", allLogs.size());
        stats.put("activeUsers", activeUsers);
        stats.put("typeCount", typeCount);
        
        return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserLogs(@PathVariable Long userId) {
        List<UserBehaviorLog> logs = userBehaviorLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(createSuccessResponse("获取成功", logs));
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}
