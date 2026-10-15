package com.movie.repository;

import com.movie.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndMovieId(Long userId, Long movieId);
    List<Rating> findByUserId(Long userId);
    List<Rating> findByMovieId(Long movieId);
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);
}
