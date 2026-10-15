package com.movie.user.repository;

import com.movie.user.entity.UserAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAuditRepository extends JpaRepository<UserAudit, Long> {
    List<UserAudit> findByStatusOrderByCreatedAtDesc(String status);
    UserAudit findByUserId(Long userId);
}
