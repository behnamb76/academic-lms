package ir.bahman.academic_lms.model;

import ir.bahman.academic_lms.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OfferedCourse extends BaseEntity<Long> {
    private DayOfWeek meetingDay;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer capacity;

    private String location;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "term_id")
    private Term term;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Person teacher;

    @ManyToMany(mappedBy = "offeredCourses", cascade = CascadeType.ALL)
    private List<Person> students = new ArrayList<>();

    @OneToMany(mappedBy = "offeredCourse")
    private List<Exam> exams = new ArrayList<>();
}
