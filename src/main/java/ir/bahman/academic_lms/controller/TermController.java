package ir.bahman.academic_lms.controller;

import ir.bahman.academic_lms.dto.AcademicCalenderDTO;
import ir.bahman.academic_lms.dto.TermDTO;
import ir.bahman.academic_lms.mapper.AcademicCalenderMapper;
import ir.bahman.academic_lms.mapper.TermMapper;
import ir.bahman.academic_lms.model.AcademicCalender;
import ir.bahman.academic_lms.model.Term;
import ir.bahman.academic_lms.service.TermService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/term")
public class TermController {
    private final TermService termService;
    private final TermMapper termMapper;
    private final AcademicCalenderMapper academicCalenderMapper;

    public TermController(TermService termService, TermMapper termMapper, AcademicCalenderMapper academicCalenderMapper) {
        this.termService = termService;
        this.termMapper = termMapper;
        this.academicCalenderMapper = academicCalenderMapper;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<TermDTO> createTerm(@Valid @RequestBody TermDTO dto) {
        Term term = termService.persist(termMapper.toEntity(dto));
        TermDTO termDTO = termMapper.toDto(term);
        return ResponseEntity.status(HttpStatus.CREATED).body(termDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<TermDTO> updateTerm(@PathVariable Long id, @Valid @RequestBody TermDTO dto) {
        Term term = termService.update(id, termMapper.toEntity(dto));
        TermDTO termDTO = termMapper.toDto(term);
        return ResponseEntity.ok().body(termDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTerm(@PathVariable Long id) {
        termService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/term-calender/{id}")
    public ResponseEntity<AcademicCalenderDTO> getTermCalender(@PathVariable Long id) {
        AcademicCalender calender = termService.findTermCalenderByTermId(id);
        return ResponseEntity.ok().body(academicCalenderMapper.toDto(calender));
    }
}
