package ir.bahman.academic_lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamInstanceRepository extends JpaRepository<ExamInstanceRepository, Long> {
}
