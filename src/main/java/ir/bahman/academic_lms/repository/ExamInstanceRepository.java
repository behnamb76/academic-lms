package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.ExamInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamInstanceRepository extends JpaRepository<ExamInstance, Long> {
}
