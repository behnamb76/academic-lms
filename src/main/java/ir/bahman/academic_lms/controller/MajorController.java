package ir.bahman.academic_lms.controller;

import ir.bahman.academic_lms.dto.MajorDTO;
import ir.bahman.academic_lms.mapper.MajorMapper;
import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.service.MajorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/major")
public class MajorController {
    private final MajorService majorService;
    private final MajorMapper majorMapper;

    public MajorController(MajorService majorService, MajorMapper majorMapper) {
        this.majorService = majorService;
        this.majorMapper = majorMapper;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MajorDTO> createMajor(@Valid @RequestBody MajorDTO dto) {
        Major major = majorService.persist(majorMapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(majorMapper.toDto(major));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<MajorDTO> updateMajor(@PathVariable Long id, @Valid @RequestBody MajorDTO dto) {
        Major major = majorService.update(id, majorMapper.toEntity(dto));
        return ResponseEntity.ok().body(majorMapper.toDto(major));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMajor(@PathVariable Long id) {
        majorService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<MajorDTO> getMajorById(@PathVariable Long id) {
        MajorDTO dto = majorMapper.toDto(majorService.findById(id));
        return ResponseEntity.ok().body(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<MajorDTO>> getAllMajors() {
        List<MajorDTO> dtoList = majorService.findAll().stream()
                .map(majorMapper::toDto)
                .toList();
        return ResponseEntity.ok().body(dtoList);
    }
}
