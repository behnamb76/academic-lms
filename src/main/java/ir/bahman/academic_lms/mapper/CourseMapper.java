package ir.bahman.academic_lms.mapper;

import ir.bahman.academic_lms.dto.CourseDTO;
import ir.bahman.academic_lms.model.Course;
import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.repository.MajorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CourseMapper implements BaseMapper<Course, CourseDTO> {
    @Autowired
    private MajorRepository majorRepository;

    public abstract CourseDTO toDto(Course entity);

    public abstract Course toEntity(CourseDTO dto);

    @AfterMapping
    protected void afterToEntity(CourseDTO dto, @MappingTarget Course course) {
        if (dto.getMajorName() != null) {
            Major major = majorRepository
                    .findByName(dto.getMajorName())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Major with name " + dto.getMajorName() + " not found"
                    ));
            course.setMajor(major);
        }
    }

    @AfterMapping
    protected void afterToDTO(Course course, @MappingTarget CourseDTO dto) {
        if (course.getMajor() != null) {
            dto.setMajorName(course.getMajor().getName());
        }
    }
}
