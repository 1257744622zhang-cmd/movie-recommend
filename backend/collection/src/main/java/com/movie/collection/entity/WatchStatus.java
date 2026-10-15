package com.movie.collection.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "watch_status", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
public class WatchStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "movie_id", nullable = false)
    private Long movieId;
    
    @Column(nullable = false, length = 20)
    private String status; // WATCHLIST, WATCHING, WATCHED
    
    @CreationTimestamp
    @Column(name = "create_time")
    private Date createTime;
    
    @UpdateTimestamp
    @Column(name = "update_time")
    private Date updateTime;
}
