package ir.bahman.academic_lms.mapper;

import ir.bahman.academic_lms.dto.OfferedCourseDTO;
import ir.bahman.academic_lms.model.*;
import ir.bahman.academic_lms.repository.CourseRepository;
import ir.bahman.academic_lms.repository.PersonRepository;
import ir.bahman.academic_lms.repository.RoleRepository;
import ir.bahman.academic_lms.repository.TermRepository;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class OfferedCourseMapper implements BaseMapper<OfferedCourse, OfferedCourseDTO> {
    @Autowired
    private TermRepository termRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private RoleRepository roleRepository;

    public abstract OfferedCourseDTO toDto(OfferedCourse entity);

    public abstract OfferedCourse toEntity(OfferedCourseDTO dto);

    @AfterMapping
    protected void afterToEntity(OfferedCourseDTO dto, @MappingTarget OfferedCourse entity) {
        if (dto.getTermId() != null) {
            Term term = termRepository.findById(dto.getTermId())
                    .orElseThrow(() -> new EntityNotFoundException("Term not found"));
            entity.setTerm(term);
        }
        if (dto.getCourseId() != null) {
            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new EntityNotFoundException("Course not found"));
            entity.setCourse(course);
        }
        if (dto.getTeacherId() != null) {
            Person teacher = personRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

            Role teacherRole = roleRepository.findByName("TEACHER")
                    .orElseThrow(() -> new EntityNotFoundException("Role not found"));
            if (!teacher.getRoles().contains(teacherRole)){
                throw new EntityNotFoundException("Person is not a teacher");
            }

            Term term = entity.getTerm();
            if (!(teacher.getMajor().equals(term.getMajor()))){
                throw new EntityNotFoundException("Teacher's major doesn't match term's major");
            }
            entity.setTeacher(teacher);
        }
    }

    @AfterMapping
    protected void afterToDTO(OfferedCourse entity, @MappingTarget OfferedCourseDTO dto) {
        if (entity.getTerm() != null) {
            dto.setTermId(entity.getTerm().getId());
        }
        if (entity.getCourse() != null) {
            dto.setCourseId(entity.getCourse().getId());
        }
        if (entity.getTeacher() != null) {
            dto.setTeacherId(entity.getTeacher().getId());
        }
    }
}
