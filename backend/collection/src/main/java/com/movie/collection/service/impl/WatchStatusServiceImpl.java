package com.movie.collection.service.impl;

import com.movie.collection.entity.WatchStatus;
import com.movie.collection.repository.WatchStatusRepository;
import com.movie.collection.service.WatchStatusService;
import com.movie.core.event.UserBehaviorEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WatchStatusServiceImpl implements WatchStatusService {

    private final WatchStatusRepository watchStatusRepository;
    private final ApplicationEventPublisher eventPublisher;

    public WatchStatusServiceImpl(WatchStatusRepository watchStatusRepository, ApplicationEventPublisher eventPublisher) {
        this.watchStatusRepository = watchStatusRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public WatchStatus setWatchStatus(Long userId, Long movieId, String status) {
        // 验证状态
        if (!isValidStatus(status)) {
            throw new RuntimeException("无效的观看状态");
        }

        // 查找现有记录
        Optional<WatchStatus> existing = watchStatusRepository.findByUserIdAndMovieId(userId, movieId);

        WatchStatus watchStatus;
        if (existing.isPresent()) {
            // 更新现有记录
            watchStatus = existing.get();
            watchStatus.setStatus(status);
        } else {
            // 创建新记录
            watchStatus = new WatchStatus();
            watchStatus.setUserId(userId);
            watchStatus.setMovieId(movieId);
            watchStatus.setStatus(status);
        }

        WatchStatus result = watchStatusRepository.save(watchStatus);

        // 发布观看状态行为事件
        eventPublisher.publishEvent(new UserBehaviorEvent(this, userId, status, movieId, null));

        return result;
    }

    @Override
    public String getWatchStatus(Long userId, Long movieId) {
        Optional<WatchStatus> watchStatus = watchStatusRepository.findByUserIdAndMovieId(userId, movieId);
        return watchStatus.map(WatchStatus::getStatus).orElse(null);
    }

    @Override
    @Transactional
    public void removeWatchStatus(Long userId, Long movieId) {
        watchStatusRepository.deleteByUserIdAndMovieId(userId, movieId);
    }

    @Override
    public List<WatchStatus> getUserWatchStatuses(Long userId) {
        return watchStatusRepository.findByUserId(userId);
    }

    @Override
    public boolean hasWatchStatus(Long userId, Long movieId) {
        return watchStatusRepository.existsByUserIdAndMovieId(userId, movieId);
    }
    
    private boolean isValidStatus(String status) {
        return status != null && (
            status.equals("WATCHLIST") ||
            status.equals("WATCHING") ||
            status.equals("WATCHED")
        );
    }
}
