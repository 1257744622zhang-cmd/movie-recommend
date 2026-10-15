package com.movie.recommend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RatingDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long movieId;
    private Double rating;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
