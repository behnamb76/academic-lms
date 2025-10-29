package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.AcademicCalender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicCalenderRepository extends JpaRepository<AcademicCalender, Long> {
}
