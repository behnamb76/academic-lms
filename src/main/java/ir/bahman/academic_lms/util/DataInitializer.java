package ir.bahman.academic_lms.util;

import ir.bahman.academic_lms.model.Account;
import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.model.Role;
import ir.bahman.academic_lms.model.enums.AccountStatus;
import ir.bahman.academic_lms.repository.AccountRepository;
import ir.bahman.academic_lms.repository.MajorRepository;
import ir.bahman.academic_lms.repository.PersonRepository;
import ir.bahman.academic_lms.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final MajorRepository majorRepository;

    public DataInitializer(RoleRepository roleRepository, PersonRepository personRepository, PasswordEncoder passwordEncoder, AccountRepository accountRepository, MajorRepository majorRepository) {
        this.roleRepository = roleRepository;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.majorRepository = majorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createRoles();
        createAdmin();
        createMajor();
    }

    public void createRoles() {
        Optional<Role> user = roleRepository.findByName("USER");
        Optional<Role> admin = roleRepository.findByName("ADMIN");
        Optional<Role> manager = roleRepository.findByName("MANAGER");
        Optional<Role> teacher = roleRepository.findByName("TEACHER");
        Optional<Role> student = roleRepository.findByName("STUDENT");
        if (student.isEmpty() && teacher.isEmpty() && admin.isEmpty() && user.isEmpty()) {
            roleRepository.save(Role.builder().name("USER").build());
            roleRepository.save(Role.builder().name("ADMIN").build());
            roleRepository.save(Role.builder().name("TEACHER").build());
            roleRepository.save(Role.builder().name("STUDENT").build());
        }
    }

    public void createAdmin() {
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));


        if (!personRepository.existsByRolesContains(adminRole)) {

            Person admin = Person.builder()
                    .firstName("Admin")
                    .lastName("Admin")
                    .nationalCode("123456789")
                    .phoneNumber("09123324213")
                    .roles(List.of(adminRole, userRole))
                    .build();

            personRepository.save(admin);


            Account account = Account.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .status(AccountStatus.ACTIVE)
                    .person(admin)
                    .activeRole(adminRole)
                    .build();

            admin.setAccount(account);
            accountRepository.save(account);
        }
    }

    public void createMajor() {
        Major computer = Major.builder().name("Computer")
                .deleted(false).majorCode(UUID.randomUUID()).build();
        if (majorRepository.findByName(computer.getName()).isEmpty()) {
            majorRepository.save(computer);
        }
    }
}
