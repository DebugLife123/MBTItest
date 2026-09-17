package com.debuglife.mbti.assessment.repository;

import com.debuglife.mbti.assessment.entity.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    List<TestAttempt> findByUserIdOrderByStartedAtDesc(Long userId);
    Optional<TestAttempt> findFirstByUserIdAndStatusOrderByStartedAtDesc(Long userId, TestAttempt.AttemptStatus status);
    List<TestAttempt> findByUserId(Long userId);
    long countByStatus(TestAttempt.AttemptStatus status);
    List<TestAttempt> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    interface AttemptCountProjection {
        Long getUserId();
        Long getAttemptCount();
    }

    @Query("select a.userId as userId, count(a) as attemptCount from TestAttempt a " +
           "where a.userId in :userIds group by a.userId")
    List<AttemptCountProjection> countByUserIds(@Param("userIds") Collection<Long> userIds);
}
