package com.movie.repository;

import com.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Page<Movie> findByGenreContainingAndReleaseYearBetweenAndRegionContaining(
            String genre, Integer yearFrom, Integer yearTo, String region, Pageable pageable);
    @Query("SELECT m FROM Movie m WHERE m.title LIKE CONCAT('%', :keyword, '%') OR m.director LIKE CONCAT('%', :keyword, '%') OR m.actors LIKE CONCAT('%', :keyword, '%')")
    Page<Movie> searchByKeyword(String keyword, Pageable pageable);
    List<Movie> findTop30ByOrderByAvgRatingDescRatingCountDesc();
    List<Movie> findByGenreOrderByAvgRatingDesc(String genre, Pageable pageable);
}
