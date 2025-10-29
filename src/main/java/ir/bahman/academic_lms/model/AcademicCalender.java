package ir.bahman.academic_lms.model;

import ir.bahman.academic_lms.model.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AcademicCalender extends BaseEntity<Long> {
    private LocalDate registrationStart;

    private LocalDate registrationEnd;

    private LocalDate classesStartDate;

    private LocalDate classesEndDate;

    @OneToMany(mappedBy = "academicCalender")
    private List<Term> terms = new ArrayList<>();
}
