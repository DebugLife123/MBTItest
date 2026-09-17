package com.debuglife.mbti.assessment.repository;

import com.debuglife.mbti.assessment.entity.TestAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestAnswerRepository extends JpaRepository<TestAnswer, Long> {
    List<TestAnswer> findByAttemptId(Long attemptId);
    void deleteByAttemptId(Long attemptId);
}
