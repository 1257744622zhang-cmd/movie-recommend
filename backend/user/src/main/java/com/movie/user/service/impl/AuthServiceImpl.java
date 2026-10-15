package com.movie.user.service.impl;

import com.movie.collection.service.CollectionFolderService;
import com.movie.core.config.JwtConfig;
import com.movie.user.dto.*;
import com.movie.user.entity.User;
import com.movie.user.repository.UserRepository;
import com.movie.user.service.AuthService;
import com.movie.user.service.UserTagService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;
    private final UserTagService userTagService;
    private final CollectionFolderService collectionFolderService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, JwtConfig jwtConfig, 
                          UserTagService userTagService,
                          CollectionFolderService collectionFolderService) {
        this.userRepository = userRepository;
        this.jwtConfig = jwtConfig;
        this.userTagService = userTagService;
        this.collectionFolderService = collectionFolderService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public UserVO register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole("USER");
        u = userRepository.save(u);
        
        // 处理观影偏好，转换为用户标签
        handleMoviePreferences(u.getId(), req.getGenrePreferences(), "genre");
        handleMoviePreferences(u.getId(), req.getDirectorPreferences(), "director");
        handleMoviePreferences(u.getId(), req.getActorPreferences(), "actor");
        
        // 创建默认收藏夹
        collectionFolderService.getOrCreateDefaultFolder(u.getId());
        
        UserVO vo = toVO(u);
        vo.setToken(jwtConfig.generateToken(u.getId(), u.getRole()));
        return vo;
    }
    
    private void handleMoviePreferences(Long userId, List<String> preferences, String type) {
        if (preferences != null && !preferences.isEmpty()) {
            for (String preference : preferences) {
                if (preference != null && !preference.trim().isEmpty()) {
                    String tagName = type + ":" + preference.trim();
                    userTagService.addOrUpdateTag(userId, tagName, 1.0);
                }
            }
        }
    }

    @Override
    public UserVO login(LoginRequest req) {
        Optional<User> opt = userRepository.findByUsername(req.getUsername());
        if (!opt.isPresent() || !passwordEncoder.matches(req.getPassword(), opt.get().getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }
        User u = opt.get();
        
        // 登录时检查并创建默认收藏夹（如果没有）
        collectionFolderService.getOrCreateDefaultFolder(u.getId());
        
        UserVO vo = toVO(u);
        vo.setToken(jwtConfig.generateToken(u.getId(), u.getRole()));
        return vo;
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    @Override
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 只更新非null的字段
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        
        return userRepository.save(user);
    }
    
    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        
        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("新密码与确认密码不一致");
        }
        
        // 检测新密码强度
        if (!isPasswordStrong(request.getNewPassword())) {
            throw new RuntimeException("新密码强度不足，至少8位，包含字母和数字");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    /**
     * 检测密码强度
     * 至少8位，包含字母和数字
     */
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }

    @Override
    public UserVO toVO(User u) {
        UserVO v = new UserVO();
        v.setId(u.getId());
        v.setUsername(u.getUsername());
        v.setRole(u.getRole());
        // 映射个人信息字段
        v.setName(u.getName());
        v.setPhone(u.getPhone());
        v.setEmail(u.getEmail());
        // 设置默认头像
        String avatar = u.getAvatar();
        if (avatar == null || avatar.trim().isEmpty()) {
            avatar = "http://localhost:8080/api/uploads/avatars/default.jpg";
        }
        v.setAvatar(avatar);
        v.setGender(u.getGender());
        v.setAddress(u.getAddress());
        v.setAge(u.getAge());
        return v;
    }
}
