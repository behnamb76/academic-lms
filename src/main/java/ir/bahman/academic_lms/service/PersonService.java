package ir.bahman.academic_lms.service;

import ir.bahman.academic_lms.dto.RegisterRequest;
import ir.bahman.academic_lms.model.Person;

public interface PersonService extends BaseService<Person, Long> {
    Person register(RegisterRequest request);

    void assignRoleToPerson(String role, Long personId);

    void changeRole(String username, String roleName);
}
