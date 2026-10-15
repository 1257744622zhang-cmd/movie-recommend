package com.movie.user.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "user_tags")
public class UserTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false, length = 100)
    private String tagName;
    private Double weight = 1.0;
}
