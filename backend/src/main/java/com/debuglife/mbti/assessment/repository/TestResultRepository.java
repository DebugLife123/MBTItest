package com.debuglife.mbti.assessment.repository;

import com.debuglife.mbti.assessment.entity.TestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    Page<TestResult> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<TestResult> findByAttemptId(Long attemptId);
}
