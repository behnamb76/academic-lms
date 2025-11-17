package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.ExamInstance;
import ir.bahman.academic_lms.model.ExamQuestion;
import ir.bahman.academic_lms.model.answer.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findByExamQuestionAndExamInstance(ExamQuestion examQuestion, ExamInstance examInstance);

    Optional<Answer> findByExamInstanceIdAndExamQuestionId(Long examInstanceId, Long examQuestionId);
}
