package ir.bahman.academic_lms.factory;

import ir.bahman.academic_lms.model.Option;
import ir.bahman.academic_lms.model.answer.Answer;
import ir.bahman.academic_lms.model.answer.EssayAnswer;
import ir.bahman.academic_lms.model.answer.TestAnswer;
import org.springframework.stereotype.Component;

@Component
public class AnswerFactory {
    public Answer createAnswer(String type , Answer answer, Option option, String answerText) {
        return switch (type.toUpperCase()) {
            case "ESSAY" -> EssayAnswer.builder()
                    .text(answerText)
                    .examQuestion(answer.getExamQuestion())
                    .examInstance(answer.getExamInstance())
                    .build();
            case "TEST" -> TestAnswer.builder()
                    .option(option)
                    .score(answer.getScore())
                    .examQuestion(answer.getExamQuestion())
                    .examInstance(answer.getExamInstance())
                    .build();
            default -> throw new IllegalArgumentException("Unknown question type: " + type + ". Supported types: DESCRIPTIVE, TEST");
        };
    }
}
