package com.movie.controller;

import com.movie.dto.LoginRequest;
import com.movie.dto.RegisterRequest;
import com.movie.dto.UserVO;
import com.movie.service.AuthService;
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
