package ir.bahman.academic_lms.service;

import ir.bahman.academic_lms.model.Exam;

import java.security.Principal;
import java.util.List;

public interface ExamService extends BaseService<Exam, Long> {
    List<Exam> findAllExamOfACourse(Long courseId);
    void startExam(Long examId, Principal principal);
    void submitExam(Long examId, Principal principal);
    void autoTestGrading(Long examId, Long studentId);
    void essayGrading(Long examId, Long studentId, Long questionId, Double score);

}
