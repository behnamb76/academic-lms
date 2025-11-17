package ir.bahman.academic_lms.controller;

import ir.bahman.academic_lms.dto.AssignRoleRequest;
import ir.bahman.academic_lms.dto.ChangeRoleRequest;
import ir.bahman.academic_lms.dto.PersonDTO;
import ir.bahman.academic_lms.dto.RegisterRequest;
import ir.bahman.academic_lms.mapper.PersonMapper;
import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.model.Role;
import ir.bahman.academic_lms.service.AccountService;
import ir.bahman.academic_lms.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/person")
public class PersonController {
    private final PersonService personService;
    private final PersonMapper personMapper;

    public PersonController(PersonService personService, PersonMapper personMapper) {
        this.personService = personService;
        this.personMapper = personMapper;
    }

    @PostMapping("/student-register")
    public ResponseEntity<PersonDTO> studentRegister(@Valid @RequestBody RegisterRequest request) {
        Person person = personService.register(request);
        personService.assignRoleToPerson("student" , person.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(personMapper.toDto(person));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/teacher-register")
    public ResponseEntity<PersonDTO> teacherRegister(@Valid @RequestBody RegisterRequest request) {
        Person person = personService.register(request);
        personService.assignRoleToPerson("teacher" , person.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(personMapper.toDto(person));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/manager-register")
    public ResponseEntity<PersonDTO> managerRegister(@Valid @RequestBody RegisterRequest request) {
        Person person = personService.register(request);
        personService.assignRoleToPerson("manager" , person.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(personMapper.toDto(person));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add-role")
    public ResponseEntity<Void> assignRoleToPerson(@Valid @RequestBody AssignRoleRequest request) {
        personService.assignRoleToPerson(request.getRole(), request.getPersonId());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STUDENT','TEACHER','USER')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody PersonDTO dto, @PathVariable Long id) {
        personService.update(id, personMapper.toEntity(dto));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<PersonDTO>> searchPeople(@PathVariable String keyword) {
        List<PersonDTO> people = personService.search(keyword)
                .stream().map(personMapper::toDto).toList();
        return ResponseEntity.ok().body(people);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/person-roles")
    public ResponseEntity<List<String>> getPersonRoles(Principal principal) {
        return ResponseEntity.status(HttpStatus.OK).body(personService.getPersonRoles(principal).stream()
                .map(Role::getName)
                .toList());
    }
}
