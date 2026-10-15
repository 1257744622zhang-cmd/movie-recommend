package com.movie.api.controller;

import com.movie.user.dto.LoginRequest;
import com.movie.user.dto.RegisterRequest;
import com.movie.user.dto.UserVO;
import com.movie.user.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public UserVO register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public UserVO login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
