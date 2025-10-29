package ir.bahman.academic_lms.model.question;

import ir.bahman.academic_lms.model.Course;
import ir.bahman.academic_lms.model.ExamQuestion;
import ir.bahman.academic_lms.model.base.BaseEntity;
import jakarta.persistence.*;
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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "question_type", discriminatorType = DiscriminatorType.STRING)
public class Question extends BaseEntity<Long> {
    private String title;

    private String text;

    private Double defaultScore;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "question")
    private List<ExamQuestion> examQuestions = new ArrayList<>();
}