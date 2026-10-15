package com.movie.movie.repository;

import com.movie.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByTitleContaining(String title);
    List<Movie> findByGenreContaining(String genre);
    List<Movie> findByDirectorContaining(String director);
    List<Movie> findByActorsContaining(String actors);
    List<Movie> findTop30ByOrderByAvgRatingDesc();
    List<Movie> findByTitleContainingOrDirectorContainingOrActorsContaining(String title, String director, String actors);
}
