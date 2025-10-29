package ir.bahman.academic_lms.model;

import ir.bahman.academic_lms.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Major extends BaseEntity<Long> {
    @Column(unique = true)
    private String name;

    private UUID majorCode;

    private boolean deleted;

    @OneToMany(mappedBy = "major")
    private List<Person> people = new ArrayList<>();

    @OneToMany(mappedBy = "major")
    private List<Term> terms = new ArrayList<>();

    @OneToMany(mappedBy = "major")
    private List<Course> courses = new ArrayList<>();
}
