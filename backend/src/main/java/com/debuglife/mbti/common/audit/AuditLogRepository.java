package com.debuglife.mbti.common.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByActionContainingIgnoreCase(String action, Pageable pageable);

    Page<AuditLog> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
