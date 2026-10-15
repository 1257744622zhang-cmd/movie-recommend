package com.movie.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    @Column(nullable = false, length = 100)
    private String password;
    @Column(length = 20)
    private String role = "USER"; // USER, ADMIN
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // 个人信息字段
    @Column(length = 50)
    private String name; // 姓名
    @Column(length = 20)
    private String phone; // 电话号码
    @Column(length = 100)
    private String email; // 邮箱
    @Column(length = 100000)
    private String avatar; // 头像URL
    @Column(length = 50)
    private String gender; // 性别
    @Column(length = 100)
    private String address; // 地址
    private Integer age; // 年龄
}

