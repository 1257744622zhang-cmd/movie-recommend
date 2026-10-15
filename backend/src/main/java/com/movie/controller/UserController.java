package com.movie.controller;

import com.movie.dto.ChangePasswordRequest;
import com.movie.dto.UpdateProfileRequest;
import com.movie.dto.UserVO;
import com.movie.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 获取当前用户的个人信息
     * 只能获取自己的信息，需要身份验证
     */
    @GetMapping("/profile")
    public UserVO getProfile(Authentication authentication) {
        // 从认证信息中获取用户ID
        Long userId = (Long) authentication.getPrincipal();
        // 获取用户信息并转换为VO
        UserVO userVO = authService.toVO(authService.getById(userId));
        return userVO;
    }

    /**
     * 更新当前用户的个人信息
     * 只能更新自己的信息，需要身份验证
     * 支持字段级别的更新，只更新非null的字段
     */
    @PutMapping("/profile")
    public UserVO updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        // 从认证信息中获取用户ID
        Long userId = (Long) authentication.getPrincipal();
        // 更新用户信息
        authService.updateProfile(userId, request);
        // 返回更新后的用户信息
        UserVO userVO = authService.toVO(authService.getById(userId));
        return userVO;
    }

    /**
     * 修改当前用户的密码
     * 只能修改自己的密码，需要身份验证
     * 包含旧密码验证、新密码强度检测及安全更新机制
     */
    @PostMapping("/change-password")
    public void changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        // 从认证信息中获取用户ID
        Long userId = (Long) authentication.getPrincipal();
        // 修改密码
        authService.changePassword(userId, request);
    }
}


