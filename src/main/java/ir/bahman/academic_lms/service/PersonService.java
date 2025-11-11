package ir.bahman.academic_lms.service;

import ir.bahman.academic_lms.dto.RegisterRequest;
import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.model.Role;

import java.security.Principal;
import java.util.List;

public interface PersonService extends BaseService<Person, Long> {
    Person register(RegisterRequest request);

    void assignRoleToPerson(String role, Long personId);

    List<Person> search(String keyword);

    List<Role> getPersonRoles(Principal principal);
}
