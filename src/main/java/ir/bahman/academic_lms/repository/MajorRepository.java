package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {
    Optional<Major> findByName(String name);

    List<Major> findByDeletedIsFalse();

    Optional<Major> findByDeletedIsFalseAndId(Long id);

    Optional<Major> findByNameAndDeletedIsFalse(String name);
}
