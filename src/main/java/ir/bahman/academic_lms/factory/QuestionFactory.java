package ir.bahman.academic_lms.factory;

import ir.bahman.academic_lms.model.Option;
import ir.bahman.academic_lms.model.question.EssayQuestion;
import ir.bahman.academic_lms.model.question.Question;
import ir.bahman.academic_lms.model.question.TestQuestion;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionFactory {
    public Question createQuestion(String type, Question question , List<Option> options) {
        switch (type.toUpperCase()) {
            case "TEST":
                TestQuestion tq = TestQuestion.builder()
                        .title(question.getTitle())
                        .text(question.getText())
                        .defaultScore(question.getDefaultScore())
                        .course(question.getCourse())
                        .options(options)
                        .build();
                if (tq.getOptions() != null) {
                    for (Option o : tq.getOptions()) {
                        o.setTestQuestion(tq);
                    }
                }
                return tq;
            case "ESSAY":
                return EssayQuestion.builder()
                        .title(question.getTitle())
                        .text(question.getText())
                        .defaultScore(question.getDefaultScore())
                        .course(question.getCourse())
                        .build();
            default:
                throw new IllegalArgumentException("Unknown question type: " + "type" + ". Valid types are: TEST, ESSAY.");
        }
    }
}
