package com.movie.collection.service;

import com.movie.collection.entity.MovieNote;
import java.util.List;

public interface MovieNoteService {
    MovieNote createNote(Long userId, Long movieId, String title, String content, String category, Boolean isPublic);

    MovieNote updateNote(Long noteId, Long userId, String title, String content, String category, Boolean isPublic, Boolean isFavorite);

    void deleteNote(Long noteId, Long userId);

    List<MovieNote> getUserNotes(Long userId);

    List<MovieNote> getUserNotesByMovie(Long userId, Long movieId);

    List<MovieNote> getPublicNotesByMovie(Long movieId);

    List<MovieNote> getUserFavoriteNotes(Long userId);

    List<MovieNote> getUserNotesByCategory(Long userId, String category);

    MovieNote getNoteById(Long noteId, Long userId);

    Long getUserNoteCount(Long userId);

    Long getUserNoteCountByMovie(Long userId, Long movieId);
}
