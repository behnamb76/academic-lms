package ir.bahman.academic_lms.model.answer;

import ir.bahman.academic_lms.model.ExamInstance;
import ir.bahman.academic_lms.model.ExamQuestion;
import ir.bahman.academic_lms.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "answer_type", discriminatorType = DiscriminatorType.STRING)
public class Answer extends BaseEntity<Long> {
    private Double score;

    @ManyToOne
    @JoinColumn(name = "exam_question_id")
    private ExamQuestion examQuestion;

    @ManyToOne
    @JoinColumn(name = "exam_instance_id")
    private ExamInstance examInstance;
}
