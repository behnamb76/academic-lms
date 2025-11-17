package ir.bahman.academic_lms.mapper;

import ir.bahman.academic_lms.dto.ExamDTO;
import ir.bahman.academic_lms.model.Exam;
import ir.bahman.academic_lms.model.OfferedCourse;
import ir.bahman.academic_lms.repository.OfferedCourseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ExamMapper implements BaseMapper<Exam, ExamDTO> {
    @Autowired
    private OfferedCourseRepository offeredCourseRepository;

    public abstract ExamDTO toDto(Exam exam);

    public abstract Exam toEntity(ExamDTO dto);

    @AfterMapping
    protected void afterToEntity(ExamDTO dto, @MappingTarget Exam exam) {
        if (dto.getCourseId() != null) {
            OfferedCourse offeredCourse = offeredCourseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new EntityNotFoundException("Course not found"));
            exam.setOfferedCourse(offeredCourse);
        }
    }

    @AfterMapping
    protected void afterToDTO(Exam exam, @MappingTarget ExamDTO dto) {
        if (exam.getOfferedCourse() != null) {
            dto.setCourseId(exam.getOfferedCourse().getId());
        }
    }
}
