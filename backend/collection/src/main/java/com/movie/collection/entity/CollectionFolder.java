package com.movie.collection.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "collection_folders", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
public class CollectionFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(length = 200)
    private String description;
    
    @Column(nullable = false, length = 20)
    private String permission = "PRIVATE"; // PRIVATE, PUBLIC
    
    @Column(name = "is_default")
    private Boolean isDefault = false; // 是否为默认收藏夹
    
    @CreationTimestamp
    @Column(name = "create_time")
    private Date createTime;
    
    @UpdateTimestamp
    @Column(name = "update_time")
    private Date updateTime;
}
