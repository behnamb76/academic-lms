package ir.bahman.academic_lms.controller;

import ir.bahman.academic_lms.dto.AssignRoleRequest;
import ir.bahman.academic_lms.dto.ChangeRoleRequest;
import ir.bahman.academic_lms.dto.PersonDTO;
import ir.bahman.academic_lms.dto.RegisterRequest;
import ir.bahman.academic_lms.mapper.PersonMapper;
import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/person")
public class PersonController {
    private final PersonService personService;
    private final PersonMapper personMapper;

    public PersonController(PersonService personService, PersonMapper personMapper) {
        this.personService = personService;
        this.personMapper = personMapper;
    }

    @PostMapping("/teacher-register")
    public ResponseEntity<PersonDTO> teacherRegister(@Valid @RequestBody RegisterRequest request) {
        Person person = personService.register(request);
        personService.assignRoleToPerson("teacher" , person.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(personMapper.toDto(person));
    }

    @PostMapping("/manager-register")
    public ResponseEntity<PersonDTO> managerRegister(@Valid @RequestBody RegisterRequest request) {
        Person person = personService.register(request);
        personService.assignRoleToPerson("manager" , person.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(personMapper.toDto(person));
    }

    @PostMapping("/add-role")
    public ResponseEntity<Void> assignRoleToPerson(@Valid @RequestBody AssignRoleRequest request) {
        personService.assignRoleToPerson(request.getRole(), request.getPersonId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody PersonDTO dto, @PathVariable Long id) {
        personService.update(id, personMapper.toEntity(dto));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-role")
    public ResponseEntity<Void> changeRole(@Valid @RequestBody ChangeRoleRequest request, Principal principal) {
        personService.changeRole(principal.getName(), request.getRole());
        return ResponseEntity.ok().build();
    }
}
