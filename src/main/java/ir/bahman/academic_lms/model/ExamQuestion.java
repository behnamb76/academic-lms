package ir.bahman.academic_lms.model;

import ir.bahman.academic_lms.model.answer.Answer;
import ir.bahman.academic_lms.model.base.BaseEntity;
import ir.bahman.academic_lms.model.question.Question;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExamQuestion extends BaseEntity<Long> {
    private Double score;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @OneToMany(mappedBy = "examQuestion")
    private List<Answer> answers = new ArrayList<>();
}
