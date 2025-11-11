package ir.bahman.academic_lms.mapper;

import ir.bahman.academic_lms.dto.TermDTO;
import ir.bahman.academic_lms.model.AcademicCalender;
import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.model.Term;
import ir.bahman.academic_lms.repository.AcademicCalenderRepository;
import ir.bahman.academic_lms.repository.MajorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class TermMapper implements BaseMapper<Term, TermDTO> {
    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private AcademicCalenderRepository academicCalenderRepository;

    public abstract TermDTO toDto(Term entity);

    public abstract Term toEntity(TermDTO dto);

    @AfterMapping
    protected void afterToEntity(TermDTO dto, @MappingTarget Term term) {
        if (dto.getMajorName() != null) {
            Major major = majorRepository.findByName(dto.getMajorName())
                    .orElseThrow(() -> new EntityNotFoundException("Major not found"));

            AcademicCalender calender = AcademicCalender.builder()
                    .registrationStart(dto.getRegistrationStart())
                    .registrationEnd(dto.getRegistrationEnd())
                    .classesStartDate(dto.getClassesStartDate())
                    .classesEndDate(dto.getClassesEndDate()).build();
            academicCalenderRepository.save(calender);
            term.setAcademicCalender(calender);
            term.setMajor(major);
        }
    }

    @AfterMapping
    protected void afterToDto(Term term, @MappingTarget TermDTO dto) {
        if (term.getMajor().getName() != null) {
            Major major = majorRepository.findByName(term.getMajor().getName())
                    .orElseThrow(() -> new EntityNotFoundException("Major not found"));
            AcademicCalender calender = term.getAcademicCalender();
            dto.setMajorName(major.getName());
            dto.setClassesEndDate(calender.getClassesEndDate());
            dto.setClassesStartDate(calender.getClassesStartDate());
            dto.setRegistrationEnd(calender.getRegistrationEnd());
            dto.setRegistrationStart(calender.getRegistrationStart());
        }
    }
}
