package ir.bahman.academic_lms.model;

import ir.bahman.academic_lms.model.base.BaseEntity;
import ir.bahman.academic_lms.model.enums.Semester;
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
public class Term extends BaseEntity<Long> {
    @Column(name = "term-year")
    private Integer year;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    private boolean deleted;

    @ManyToOne
    @JoinColumn(name = "academic_calender_id")
    private AcademicCalender academicCalender;

    @ManyToOne
    @JoinColumn(name = "major_id")
    private Major major;

    @OneToMany(mappedBy = "term")
    private List<OfferedCourse> offeredCourses = new ArrayList<>();
}
