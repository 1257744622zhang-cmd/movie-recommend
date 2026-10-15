package com.movie.service;

import com.movie.entity.Movie;
import com.movie.entity.Rating;
import com.movie.repository.MovieRepository;
import com.movie.repository.RatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;

    public RatingService(RatingRepository ratingRepository, MovieRepository movieRepository) {
        this.ratingRepository = ratingRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional
    public Rating rate(Long userId, Long movieId, Double score) {
        if (score < 0 || score > 10) throw new RuntimeException("评分需在0-10之间");
        Optional<Rating> opt = ratingRepository.findByUserIdAndMovieId(userId, movieId);
        Rating r = opt.orElseGet(() -> {
            Rating nr = new Rating();
            nr.setUserId(userId);
            nr.setMovieId(movieId);
            return nr;
        });
        r.setScore(score);
        r = ratingRepository.save(r);
        updateMovieAvgRating(movieId);
        return r;
    }

    private void updateMovieAvgRating(Long movieId) {
        List<Rating> list = ratingRepository.findByMovieId(movieId);
        if (list.isEmpty()) return;
        double avg = list.stream().mapToDouble(Rating::getScore).average().orElse(0);
        Movie m = movieRepository.findById(movieId).orElse(null);
        if (m != null) {
            m.setAvgRating(Math.round(avg * 10) / 10.0);
            m.setRatingCount(list.size());
            movieRepository.save(m);
        }
    }

    public Optional<Double> getMyScore(Long userId, Long movieId) {
        return ratingRepository.findByUserIdAndMovieId(userId, movieId).map(Rating::getScore);
    }

    public List<Long> getRatedMovieIds(Long userId) {
        return ratingRepository.findByUserId(userId).stream()
                .map(Rating::getMovieId).collect(Collectors.toList());
    }
}
