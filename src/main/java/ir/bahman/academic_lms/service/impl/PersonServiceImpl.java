package ir.bahman.academic_lms.service.impl;

import ir.bahman.academic_lms.dto.RegisterRequest;
import ir.bahman.academic_lms.exception.AccessDeniedException;
import ir.bahman.academic_lms.exception.AlreadyExistsException;
import ir.bahman.academic_lms.model.Account;
import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.model.Role;
import ir.bahman.academic_lms.model.enums.AccountStatus;
import ir.bahman.academic_lms.repository.AccountRepository;
import ir.bahman.academic_lms.repository.MajorRepository;
import ir.bahman.academic_lms.repository.PersonRepository;
import ir.bahman.academic_lms.repository.RoleRepository;
import ir.bahman.academic_lms.service.PersonService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PersonServiceImpl extends BaseServiceImpl<Person, Long> implements PersonService {

    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final MajorRepository majorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    public PersonServiceImpl(JpaRepository<Person, Long> repository, PersonRepository personRepository, RoleRepository roleRepository, MajorRepository majorRepository, PasswordEncoder passwordEncoder, AccountRepository accountRepository) {
        super(repository);
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
        this.majorRepository = majorRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }

    @Override
    protected void prePersist(Person person) {
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));
        if (personRepository.existsByNationalCodeAndPhoneNumber(person.getNationalCode() , person.getPhoneNumber())) {
            throw new AlreadyExistsException("This person already exists!");
        }

        List<Role> roles = new ArrayList<>();
        roles.add(role);
        person.setRoles(roles);

        Major major = majorRepository.findByName(person.getMajor().getName())
                .orElseThrow(() -> new EntityNotFoundException("Major not found!"));
        person.setMajor(major);
    }

    @Override
    protected void postPersist(Person person) {
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));

        Account account = person.getAccount();

        if (account != null) {
            String rawPassword = account.getPassword();
            account.setPassword(passwordEncoder.encode(rawPassword));
            account.setStatus(AccountStatus.PENDING);
            account.setPerson(person);
            account.setActiveRole(role);
        }

        Person savedPerson = personRepository.save(person);

        if (account != null && account.getId() == null) {
            accountRepository.save(account);
        }
    }

    @Override
    public Person register(RegisterRequest request) {
        Account account = Account.builder()
                .username(request.getUsername())
                .password(request.getPassword()).build();

        Person person = Person.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .nationalCode(request.getNationalCode())
                .phoneNumber(request.getPhoneNumber())
                .account(account)
                .major(majorRepository.findByName(request.getMajorName())
                        .orElseThrow(() -> new EntityNotFoundException("Major not found!"))).build();

        return persist(person);
    }

    @Override
    public Person update(Long id, Person person) {
        Person foundedPerson = personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found!"));

        foundedPerson.setFirstName(person.getFirstName());
        foundedPerson.setLastName(person.getLastName());
        foundedPerson.setNationalCode(person.getNationalCode());
        foundedPerson.setPhoneNumber(person.getPhoneNumber());

        return personRepository.save(foundedPerson);
    }

    @Override
    public void assignRoleToPerson(String role, Long personId) {
        Role founded = roleRepository.findByName(role.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));

        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found!"));
        person.getRoles().add(founded);
        personRepository.save(person);
    }

    @Override
    public List<Person> search(String keyword) {
        return personRepository.searchByKeyword(keyword);
    }

    @Override
    public List<Role> getPersonRoles(Principal principal) {
        String username = principal.getName();
        Person person= personRepository.findByAccountUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));
        return person.getRoles();
    }
}
