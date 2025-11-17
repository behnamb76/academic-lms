package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByDeletedIsFalse();

    Optional<Exam> findByOfferedCourse_Id(Long offeredCourseId);
}
