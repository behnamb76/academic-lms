package ir.bahman.academic_lms.service;

import ir.bahman.academic_lms.model.Option;
import ir.bahman.academic_lms.model.answer.Answer;

public interface AnswerService extends BaseService<Answer, Long> {
    void saveAnswer(String type, Answer answer, Option option, String answerText);
    Option findOptionById(Long id);
}
