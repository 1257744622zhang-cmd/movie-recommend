package com.movie.service;

import com.movie.entity.Movie;
import com.movie.repository.MovieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Page<Movie> search(String keyword, String genre, Integer yearFrom, Integer yearTo, String region, int page, int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return movieRepository.searchByKeyword(keyword.trim(), PageRequest.of(page, size));
        }
        int yf = yearFrom != null ? yearFrom : 1900;
        int yt = yearTo != null ? yearTo : 2100;
        String g = genre != null && !genre.isEmpty() ? genre : "";
        String r = region != null && !region.isEmpty() ? region : "";
        return movieRepository.findByGenreContainingAndReleaseYearBetweenAndRegionContaining(
                g, yf, yt, r, PageRequest.of(page, size));
    }

    public Optional<Movie> getById(Long id) {
        return movieRepository.findById(id);
    }

    public Movie save(Movie movie) {
        return movieRepository.save(movie);
    }

    public void delete(Long id) {
        movieRepository.deleteById(id);
    }

    public List<Movie> getPopularTop30() {
        return movieRepository.findTop30ByOrderByAvgRatingDescRatingCountDesc();
    }

    public List<Movie> getByGenre(String genre, int limit) {
        return movieRepository.findByGenreOrderByAvgRatingDesc(genre, PageRequest.of(0, limit));
    }
}
