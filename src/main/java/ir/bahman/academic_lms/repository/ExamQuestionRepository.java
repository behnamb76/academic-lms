package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.Exam;
import ir.bahman.academic_lms.model.ExamQuestion;
import ir.bahman.academic_lms.model.question.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    Optional<ExamQuestion> findByExamIdAndQuestionId(Long examId, Long questionId);

    Optional<ExamQuestion> findByExamAndQuestion(Exam exam, Question question);
}
