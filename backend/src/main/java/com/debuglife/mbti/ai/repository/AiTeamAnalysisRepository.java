package com.debuglife.mbti.ai.repository;

import com.debuglife.mbti.ai.entity.AiTeamAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiTeamAnalysisRepository extends JpaRepository<AiTeamAnalysis, Long> {
    List<AiTeamAnalysis> findTop20ByOrderByCreatedAtDesc();
}