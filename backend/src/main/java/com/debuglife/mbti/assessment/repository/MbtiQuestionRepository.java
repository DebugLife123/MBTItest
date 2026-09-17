package com.debuglife.mbti.assessment.repository;

import com.debuglife.mbti.assessment.entity.MbtiQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MbtiQuestionRepository extends JpaRepository<MbtiQuestion, Long> {
    List<MbtiQuestion> findAllByOrderBySortOrderAsc();
}
