package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.Course;
import ir.bahman.academic_lms.model.OfferedCourse;
import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.model.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfferedCourseRepository extends JpaRepository<OfferedCourse, Long> {
    @Query("""
                select (count(oc) > 0)
                from OfferedCourse oc
                where oc.teacher = :teacher
                  and oc.term = :term
                  and oc.meetingDay = :meetingDay
                  and oc.startTime < :endTime
                  and oc.endTime > :startTime
            """)
    boolean existsOverlappingCourse(@Param("teacher") Person teacher,
                                    @Param("term") Term term,
                                    @Param("startTime") LocalTime startTime,
                                    @Param("meetingDay") DayOfWeek meetingDay,
                                    @Param("endTime") LocalTime endTime);

    List<OfferedCourse> findAllByTeacher(Person teacher);

    List<OfferedCourse> findByStudents_Id(Long studentId);

    List<OfferedCourse> findAllByTerm(Term term);

    Optional<Object> findByCourse(Course course);

    OfferedCourse findByCourse_Title(String courseTitle);
}
