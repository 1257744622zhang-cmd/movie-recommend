package com.movie.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "collections", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id", "collection_folder"}))
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "movie_id", nullable = false)
    private Long movieId;
    @Column(name = "collection_folder", nullable = false, length = 20)
    private String collectionFolder = "默认收藏夹";
    @Column(length = 20)
    private String status = "WANT"; // WANT, WATCHING, WATCHED
    private Boolean isPublic = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}
