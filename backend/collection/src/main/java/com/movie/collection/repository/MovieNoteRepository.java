package com.movie.collection.repository;

import com.movie.collection.entity.MovieNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieNoteRepository extends JpaRepository<MovieNote, Long> {
    List<MovieNote> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<MovieNote> findByUserIdAndMovieIdOrderByUpdatedAtDesc(Long userId, Long movieId);

    List<MovieNote> findByMovieIdAndIsPublicTrueOrderByUpdatedAtDesc(Long movieId);

    List<MovieNote> findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(Long userId);

    List<MovieNote> findByUserIdAndCategoryOrderByUpdatedAtDesc(Long userId, String category);

    Long countByUserId(Long userId);

    Long countByUserIdAndMovieId(Long userId, Long movieId);
}
