package com.movie.dto;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String role;
    private String token;
    
    // 个人信息字段
    private String name; // 姓名
    private String phone; // 电话号码
    private String email; // 邮箱
    private String avatar; // 头像URL
    private String gender; // 性别
    private String address; // 地址
    private Integer age; // 年龄
}

