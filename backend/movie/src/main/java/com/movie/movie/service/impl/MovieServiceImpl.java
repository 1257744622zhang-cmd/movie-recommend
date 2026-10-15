package com.movie.movie.service.impl;

import com.movie.movie.dto.MovieVO;
import com.movie.movie.entity.Movie;
import com.movie.movie.repository.MovieRepository;
import com.movie.movie.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Autowired
    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public List<MovieVO> getHotMovies() {
        return movieRepository.findTop30ByOrderByAvgRatingDesc().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public MovieVO getMovieById(Long id) {
        return movieRepository.findById(id)
                .map(this::toVO)
                .orElse(null);
    }

    @Override
    public List<MovieVO> searchMovies(String keyword) {
        return movieRepository.findByTitleContainingOrDirectorContainingOrActorsContaining(keyword, keyword, keyword).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieVO> filterMovies(String genre, String director, String actor) {
        List<Movie> movies = movieRepository.findAll();
        return movies.stream()
                .filter(m -> (genre == null || m.getGenre() != null && m.getGenre().contains(genre)))
                .filter(m -> (director == null || m.getDirector() != null && m.getDirector().contains(director)))
                .filter(m -> (actor == null || m.getActors() != null && m.getActors().contains(actor)))
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieVO> filterMoviesAdvanced(String genre, Double minRating, 
                                                Integer startYear, Integer endYear, String region) {
        List<Movie> movies = movieRepository.findAll();
        System.out.println("总电影数: " + movies.size());
        
        List<Movie> filtered = movies.stream()
                .filter(movie -> {
                    boolean pass = genre == null || (movie.getGenre() != null && movie.getGenre().contains(genre));
                    if (!pass && genre != null) {
                        System.out.println("类型不匹配: " + movie.getTitle() + " - " + movie.getGenre());
                    }
                    return pass;
                })
                .filter(movie -> {
                    boolean pass = minRating == null || (movie.getAvgRating() != null && movie.getAvgRating() >= minRating);
                    if (!pass && minRating != null) {
                        System.out.println("评分不匹配: " + movie.getTitle() + " - " + movie.getAvgRating());
                    }
                    return pass;
                })
                .filter(movie -> {
                    boolean pass = startYear == null || (movie.getReleaseYear() != null && movie.getReleaseYear() >= startYear);
                    if (!pass && startYear != null) {
                        System.out.println("起始年份不匹配: " + movie.getTitle() + " - " + movie.getReleaseYear());
                    }
                    return pass;
                })
                .filter(movie -> {
                    boolean pass = endYear == null || (movie.getReleaseYear() != null && movie.getReleaseYear() <= endYear);
                    if (!pass && endYear != null) {
                        System.out.println("结束年份不匹配: " + movie.getTitle() + " - " + movie.getReleaseYear());
                    }
                    return pass;
                })
                .filter(movie -> {
                    boolean pass = region == null || (movie.getRegion() != null && movie.getRegion().contains(region));
                    if (!pass && region != null) {
                        System.out.println("地区不匹配: " + movie.getTitle() + " - " + movie.getRegion());
                    }
                    return pass;
                })
                .collect(Collectors.toList());
        
        System.out.println("筛选后电影数: " + filtered.size());
        return filtered.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public MovieVO saveMovie(Movie movie) {
        return toVO(movieRepository.save(movie));
    }

    @Override
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }

    @Override
    public List<MovieVO> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<MovieVO> getAllMoviesPageable(Pageable pageable) {
        return movieRepository.findAll(pageable)
                .map(this::toVO);
    }

    @Override
    public List<MovieVO> getMoviesByGenre(String genre, int limit) {
        return movieRepository.findByGenreContaining(genre).stream()
                .limit(limit)
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public MovieVO toVO(Movie movie) {
        if (movie == null) return null;
        MovieVO vo = new MovieVO();
        vo.setId(movie.getId());
        vo.setTitle(movie.getTitle());
        vo.setGenre(movie.getGenre());
        vo.setDirector(movie.getDirector());
        vo.setActors(movie.getActors());
        vo.setReleaseYear(movie.getReleaseYear());
        vo.setRegion(movie.getRegion());
        vo.setDescription(movie.getDescription());
        vo.setPoster(movie.getPoster());
        vo.setAvgRating(movie.getAvgRating());
        vo.setRatingCount(movie.getRatingCount());
        return vo;
    }
}
