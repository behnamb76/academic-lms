package ir.bahman.academic_lms.model;

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
public class Person extends BaseEntity<Long> {
    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String nationalCode;

    @Column(unique = true)
    private String phoneNumber;

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL)
    private Account account;

    @OneToMany(mappedBy = "person")
    private List<ExamInstance> exams = new ArrayList<>();

    @ManyToOne
    private Major major;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "person_roles",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "person_offered_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "offered_course_id")
    )
    private List<OfferedCourse> offeredCourses = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "offered_course_id")
    private OfferedCourse offeredCourse;
}
