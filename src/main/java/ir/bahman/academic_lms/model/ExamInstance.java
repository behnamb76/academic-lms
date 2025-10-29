package ir.bahman.academic_lms.model;

import ir.bahman.academic_lms.model.answer.Answer;
import ir.bahman.academic_lms.model.base.BaseEntity;
import ir.bahman.academic_lms.model.enums.ExamInstanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExamInstance extends BaseEntity<Long> {
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    private ExamInstanceStatus examInstanceStatus;

    private Double totalScore;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @OneToMany(mappedBy = "examInstance")
    private List<Answer> answers = new ArrayList<>();
}
