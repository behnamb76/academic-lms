package ir.bahman.academic_lms.controller;

import ir.bahman.academic_lms.dto.OfferedCourseDTO;
import ir.bahman.academic_lms.dto.OfferedCourseResponseDTO;
import ir.bahman.academic_lms.mapper.OfferedCourseMapper;
import ir.bahman.academic_lms.mapper.OfferedCourseResponseMapper;
import ir.bahman.academic_lms.model.OfferedCourse;
import ir.bahman.academic_lms.service.OfferedCourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/offered-course")
public class OfferedCourseController {
    private final OfferedCourseService offeredCourseService;
    private final OfferedCourseResponseMapper respMapper;
    private final OfferedCourseMapper mapper;

    public OfferedCourseController(OfferedCourseService offeredCourseService, OfferedCourseResponseMapper respMapper, OfferedCourseMapper mapper) {
        this.offeredCourseService = offeredCourseService;
        this.respMapper = respMapper;
        this.mapper = mapper;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<OfferedCourseResponseDTO> createOfferedCourse(@Valid @RequestBody OfferedCourseDTO dto) {
        OfferedCourse offeredCourse = offeredCourseService.persist(mapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(respMapper.toDto(offeredCourse));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<OfferedCourseResponseDTO> updateOfferedCourse(@PathVariable Long id,@Valid @RequestBody OfferedCourseDTO dto) {
        OfferedCourse updated = offeredCourseService.update(id, mapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.OK).body(respMapper.toDto(updated));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOfferedCourse(@PathVariable Long id) {
        offeredCourseService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<OfferedCourseResponseDTO> findOfferedCourseById(@PathVariable Long id) {
        OfferedCourseResponseDTO dto = respMapper.toDto(offeredCourseService.findById(id));
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    public ResponseEntity<List<OfferedCourseResponseDTO>> findAllOfferedCourses() {
        List<OfferedCourseResponseDTO> dtoList = offeredCourseService.findAll().stream()
                .map(respMapper::toDto)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(dtoList);
    }


    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/teacher-courses")
    public ResponseEntity<List<OfferedCourseResponseDTO>> findAllTeacherCourses(Principal principal) {
        List<OfferedCourseResponseDTO> dtoList = offeredCourseService.findAllTeacherCourse(principal).stream()
                .map(respMapper::toDto)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(dtoList);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student-courses")
    public ResponseEntity<List<OfferedCourseResponseDTO>> findAllStudentCourses(Principal principal) {
        List<OfferedCourseResponseDTO> dtoList = offeredCourseService.findAllStudentCourses(principal).stream()
                .map(respMapper::toDto)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(dtoList);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEACHER','STUDENT')")
    @GetMapping("/term-courses/{termId}")
    public ResponseEntity<List<OfferedCourseResponseDTO>> findAllTermCourses(@PathVariable Long termId, Principal principal) {
        List<OfferedCourseResponseDTO> dtoList = offeredCourseService.findAllTermCourses(termId, principal).stream()
                .map(respMapper::toDto)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(dtoList);
    }
}
