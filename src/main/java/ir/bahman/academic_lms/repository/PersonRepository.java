package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    boolean existsByNationalCodeAndPhoneNumber(String nationalCode, String phoneNumber);

    boolean existsByRolesContains(Role role);

    Optional<Person> findByNationalCode(String nationalCode);

    @Query("""
            SELECT DISTINCT p FROM Person p
            LEFT JOIN p.account a
            LEFT JOIN p.major m
            LEFT JOIN p.roles r
            WHERE
                LOWER(p.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(p.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                a.username LIKE CONCAT('%', :keyword, '%') OR
                LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<Person> searchByKeyword(@Param("keyword") String keyword);

    Optional<Person> findByAccountUsername(String username);

    boolean existsByIdAndOfferedCourses_Id(Long id, Long offeredCourseId);
}
