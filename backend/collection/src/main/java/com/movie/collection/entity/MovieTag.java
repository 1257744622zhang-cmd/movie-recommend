package com.movie.collection.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "movie_tags", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"movie_id", "tag_id"}, name = "uk_movie_tag"),
       indexes = {
           @Index(name = "idx_movie_id", columnList = "movie_id"),
           @Index(name = "idx_tag_id", columnList = "tag_id")
       })
public class MovieTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "movie_id", nullable = false)
    private Long movieId;
    
    @Column(name = "tag_id", nullable = false)
    private Long tagId;
    
    @CreationTimestamp
    @Column(name = "create_time")
    private Date createTime;
}
