package com.movie.user.service;

import com.movie.user.dto.*;
import com.movie.user.entity.User;

public interface AuthService {
    UserVO register(RegisterRequest req);
    UserVO login(LoginRequest req);
    User getById(Long id);
    User updateProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    UserVO toVO(User u);
}
