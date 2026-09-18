package com.debuglife.mbti.ai.repository;

import com.debuglife.mbti.ai.entity.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {
    List<AiChatSession> findTop50ByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<AiChatSession> findByIdAndUserId(Long id, Long userId);
}