package ir.bahman.academic_lms.model;

import ir.bahman.academic_lms.model.answer.TestAnswer;
import ir.bahman.academic_lms.model.base.BaseEntity;
import ir.bahman.academic_lms.model.question.TestQuestion;
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
public class Option extends BaseEntity<Long> {
    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private boolean correct;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private TestQuestion testQuestion;

    @OneToMany(mappedBy = "option")
    private List<TestAnswer> testAnswers = new ArrayList<>();
}
