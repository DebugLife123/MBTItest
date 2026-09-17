package com.debuglife.mbti.assessment.repository;

import com.debuglife.mbti.assessment.entity.MbtiPersonality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MbtiPersonalityRepository extends JpaRepository<MbtiPersonality, Long> {
    Optional<MbtiPersonality> findByTypeCode(String typeCode);
}
