package com.movie.user.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "user_preferences")
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(length = 500)
    private String favoriteGenres; // 喜欢的类型，逗号分隔

    @Column(length = 500)
    private String favoriteDirectors; // 喜欢的导演，逗号分隔

    @Column(length = 500)
    private String favoriteActors; // 喜欢的演员，逗号分隔
}
