package ir.bahman.academic_lms.repository;

import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    boolean existsByNationalCodeAndPhoneNumber(String nationalCode, String phoneNumber);

    boolean existsByRolesContains(Role role);

    Optional<Person> findByNationalCode(String nationalCode);
}
