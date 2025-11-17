package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.Exam;
import ir.bahman.academic_lms.model.ExamInstance;
import ir.bahman.academic_lms.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamInstanceRepository extends JpaRepository<ExamInstance, Long> {
    Optional<ExamInstance> findByPersonAndExam(Person person, Exam exam);
}
