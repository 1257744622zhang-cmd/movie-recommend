package com.movie.collection.service.impl;

import com.movie.collection.entity.MovieNote;
import com.movie.collection.repository.MovieNoteRepository;
import com.movie.collection.service.MovieNoteService;
import com.movie.core.event.UserBehaviorEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieNoteServiceImpl implements MovieNoteService {

    private final MovieNoteRepository movieNoteRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MovieNoteServiceImpl(MovieNoteRepository movieNoteRepository, ApplicationEventPublisher eventPublisher) {
        this.movieNoteRepository = movieNoteRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public MovieNote createNote(Long userId, Long movieId, String title, String content, String category, Boolean isPublic) {
        MovieNote note = new MovieNote();
        note.setUserId(userId);
        note.setMovieId(movieId);
        note.setTitle(title);
        note.setContent(content);
        note.setCategory(category != null ? category : "GENERAL");
        note.setIsPublic(isPublic != null ? isPublic : false);
        note.setIsFavorite(false);
        MovieNote savedNote = movieNoteRepository.save(note);
        // 发布笔记行为事件
        eventPublisher.publishEvent(new UserBehaviorEvent(this, userId, "NOTE", movieId, category));
        return savedNote;
    }

    @Override
    public MovieNote updateNote(Long noteId, Long userId, String title, String content, String category, Boolean isPublic, Boolean isFavorite) {
        MovieNote note = movieNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此笔记");
        }

        if (title != null) note.setTitle(title);
        if (content != null) note.setContent(content);
        if (category != null) note.setCategory(category);
        if (isPublic != null) note.setIsPublic(isPublic);
        if (isFavorite != null) note.setIsFavorite(isFavorite);

        return movieNoteRepository.save(note);
    }

    @Override
    public void deleteNote(Long noteId, Long userId) {
        MovieNote note = movieNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此笔记");
        }

        movieNoteRepository.delete(note);
    }

    @Override
    public List<MovieNote> getUserNotes(Long userId) {
        return movieNoteRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Override
    public List<MovieNote> getUserNotesByMovie(Long userId, Long movieId) {
        return movieNoteRepository.findByUserIdAndMovieIdOrderByUpdatedAtDesc(userId, movieId);
    }

    @Override
    public List<MovieNote> getPublicNotesByMovie(Long movieId) {
        return movieNoteRepository.findByMovieIdAndIsPublicTrueOrderByUpdatedAtDesc(movieId);
    }

    @Override
    public List<MovieNote> getUserFavoriteNotes(Long userId) {
        return movieNoteRepository.findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(userId);
    }

    @Override
    public List<MovieNote> getUserNotesByCategory(Long userId, String category) {
        return movieNoteRepository.findByUserIdAndCategoryOrderByUpdatedAtDesc(userId, category);
    }

    @Override
    public MovieNote getNoteById(Long noteId, Long userId) {
        MovieNote note = movieNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        // 只能查看自己的笔记或公开的笔记
        if (!note.getUserId().equals(userId) && !note.getIsPublic()) {
            throw new RuntimeException("无权查看此笔记");
        }

        return note;
    }

    @Override
    public Long getUserNoteCount(Long userId) {
        return movieNoteRepository.countByUserId(userId);
    }

    @Override
    public Long getUserNoteCountByMovie(Long userId, Long movieId) {
        return movieNoteRepository.countByUserIdAndMovieId(userId, movieId);
    }
}
