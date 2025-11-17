package ir.bahman.academic_lms.mapper;

import ir.bahman.academic_lms.dto.OfferedCourseResponseDTO;
import ir.bahman.academic_lms.model.OfferedCourse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class OfferedCourseResponseMapper implements BaseMapper<OfferedCourse, OfferedCourseResponseDTO> {
    public abstract OfferedCourseResponseDTO toDto(OfferedCourse entity);

    public abstract OfferedCourse toEntity(OfferedCourseResponseDTO dto);


    @AfterMapping
    protected void afterToDTO(OfferedCourse entity, @MappingTarget OfferedCourseResponseDTO dto) {
        if (entity.getTerm() != null) {
            dto.setTermId(entity.getTerm().getId());
        }
        if (entity.getCourse() != null) {
            dto.setCourseTitle(entity.getCourse().getTitle());
        }
        if (entity.getTeacher() != null) {
            dto.setTeacherName(entity.getTeacher().getFirstName() + " " + entity.getTeacher().getLastName());
        }
        if (entity.getTerm() != null) {
            dto.setMajorName(entity.getTerm().getMajor().getName());
        }
    }
}
