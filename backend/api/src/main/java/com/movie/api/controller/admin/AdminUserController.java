package com.movie.api.controller.admin;

import com.movie.user.entity.User;
import com.movie.user.entity.UserAudit;
import com.movie.user.repository.UserAuditRepository;
import com.movie.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserAuditRepository userAuditRepository;

    public AdminUserController(UserRepository userRepository, UserAuditRepository userAuditRepository) {
        this.userRepository = userRepository;
        this.userAuditRepository = userAuditRepository;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAll(pageRequest);
        return ResponseEntity.ok(createSuccessResponse("获取成功", users));
    }

    @GetMapping("/audit/list")
    public ResponseEntity<?> getAuditList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "PENDING") String status) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return ResponseEntity.ok(createSuccessResponse("获取成功", 
            userAuditRepository.findByStatusOrderByCreatedAtDesc(status)));
    }

    @PostMapping("/audit/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id, Authentication authentication) {
        UserAudit audit = userAuditRepository.findById(id).orElse(null);
        if (audit == null) {
            return ResponseEntity.badRequest().body(createErrorResponse("审核记录不存在"));
        }
        audit.setStatus("APPROVED");
        audit.setApprovedBy((Long) authentication.getPrincipal());
        userAuditRepository.save(audit);
        return ResponseEntity.ok(createSuccessResponse("审核通过", null));
    }

    @PostMapping("/audit/{id}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable Long id, @RequestBody Map<String, String> request, Authentication authentication) {
        UserAudit audit = userAuditRepository.findById(id).orElse(null);
        if (audit == null) {
            return ResponseEntity.badRequest().body(createErrorResponse("审核记录不存在"));
        }
        audit.setStatus("REJECTED");
        audit.setRejectReason(request.get("reason"));
        audit.setApprovedBy((Long) authentication.getPrincipal());
        userAuditRepository.save(audit);
        return ResponseEntity.ok(createSuccessResponse("审核拒绝", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(createSuccessResponse("删除用户成功", null));
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
