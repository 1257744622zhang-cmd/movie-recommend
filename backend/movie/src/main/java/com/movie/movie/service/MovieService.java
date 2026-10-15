package com.movie.movie.service;

import com.movie.movie.dto.MovieVO;
import com.movie.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MovieService {
    List<MovieVO> getHotMovies();
    MovieVO getMovieById(Long id);
    List<MovieVO> searchMovies(String keyword);
    List<MovieVO> filterMovies(String genre, String director, String actor);
    List<MovieVO> filterMoviesAdvanced(String genre, Double minRating, 
                                        Integer startYear, Integer endYear, String region);
    MovieVO toVO(Movie movie);
    
    MovieVO saveMovie(Movie movie);
    void deleteMovie(Long id);
    List<MovieVO> getAllMovies();
    Page<MovieVO> getAllMoviesPageable(Pageable pageable);
    
    List<MovieVO> getMoviesByGenre(String genre, int limit);
}
