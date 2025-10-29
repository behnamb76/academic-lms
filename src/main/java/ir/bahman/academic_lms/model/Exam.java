package ir.bahman.academic_lms.model;

import ir.bahman.academic_lms.model.base.BaseEntity;
import ir.bahman.academic_lms.model.enums.ExamState;
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
public class Exam extends BaseEntity<Long> {
    private String title;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Double score;

    @Enumerated(EnumType.STRING)
    private ExamState examState;

    private boolean deleted;

    @ManyToOne
    @JoinColumn(name = "offered_course_id")
    private OfferedCourse offeredCourse;

    @OneToMany(mappedBy = "exam")
    private List<ExamQuestion> examQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "exam")
    private List<ExamInstance> examInstances =new ArrayList<>();
}
