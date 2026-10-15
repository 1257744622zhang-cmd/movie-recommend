package com.movie.collection.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "collections")
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long movieId;
    @Column(length = 20)
    private String status = "COLLECTED"; // COLLECTED, WATCHLIST, WATCHED, WATCHING
    @Column(length = 50)
    private String collectionName = "默认收藏夹";
    @Column(length = 20)
    private String permission = "PRIVATE"; // PRIVATE, PUBLIC
    @Column(name = "folder_id")
    private Long folderId; // 关联的收藏夹ID
    @Column(name = "is_collected")
    private Boolean isCollected = true; // 是否已收藏（收藏状态和观看状态可以叠加）
}
