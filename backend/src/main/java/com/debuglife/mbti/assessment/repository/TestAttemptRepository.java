package com.debuglife.mbti.assessment.repository;

import com.debuglife.mbti.assessment.entity.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    List<TestAttempt> findByUserIdOrderByStartedAtDesc(Long userId);
    Optional<TestAttempt> findFirstByUserIdAndStatusOrderByStartedAtDesc(Long userId, TestAttempt.AttemptStatus status);
}
