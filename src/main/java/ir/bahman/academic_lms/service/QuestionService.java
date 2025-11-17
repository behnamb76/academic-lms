package ir.bahman.academic_lms.service;

import ir.bahman.academic_lms.model.Option;
import ir.bahman.academic_lms.model.question.Question;

import java.security.Principal;
import java.util.List;

public interface QuestionService extends BaseService<Question, Long>{
    Question create(String type, Question question, List<Option> options);
    List<Question> findQuestionsByExamId(Long examId);
    List<Question> findQuestionsOfCourse(Long courseId, Principal principal);
    void assignQuestionToExam(Long examId, Long questionId, Double score);
}
