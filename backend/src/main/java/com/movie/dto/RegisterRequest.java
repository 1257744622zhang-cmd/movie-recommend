package com.movie.dto;

import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private List<String> genrePreferences;
    private List<String> directorPreferences;
    private List<String> actorPreferences;
}
