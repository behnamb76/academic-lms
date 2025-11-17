package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.Course;
import ir.bahman.academic_lms.model.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByTitleAndMajor(String title, Major major);

    List<Course> findByMajorAndDeletedIsFalse(Major major);

    List<Course> findByDeletedIsFalse();

    Optional<Course> findByTitle(String title);
}
