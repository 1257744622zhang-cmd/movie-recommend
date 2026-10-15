package com.movie.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "user_tags", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tag_name"}))
public class UserTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;
    private Double weight = 1.0;
}
