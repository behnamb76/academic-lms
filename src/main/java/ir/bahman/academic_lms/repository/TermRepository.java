package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.model.Term;
import ir.bahman.academic_lms.model.enums.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {
    boolean existsBySemesterAndMajor(Semester semester, Major major);

    List<Term> findAllByDeletedIsFalse();
}
