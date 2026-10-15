package com.movie.recommend.repository;

import com.movie.recommend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByUserId(Long userId);
    List<Rating> findByMovieId(Long movieId);
    Rating findByUserIdAndMovieId(Long userId, Long movieId);
}
