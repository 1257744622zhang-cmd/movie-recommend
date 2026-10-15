package com.movie.collection.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "tags", indexes = {
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_usage_count", columnList = "usage_count")
})
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(length = 20)
    private String color = "#409EFF";
    
    @Column(length = 200)
    private String description;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @CreationTimestamp
    @Column(name = "create_time")
    private Date createTime;
    
    @UpdateTimestamp
    @Column(name = "update_time")
    private Date updateTime;
}
