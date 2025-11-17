package ir.bahman.academic_lms.service.impl;

import ir.bahman.academic_lms.exception.AlreadyExistsException;
import ir.bahman.academic_lms.model.Course;
import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.repository.CourseRepository;
import ir.bahman.academic_lms.repository.MajorRepository;
import ir.bahman.academic_lms.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl extends BaseServiceImpl<Course, Long> implements CourseService {
    private final CourseRepository courseRepository;
    private final MajorRepository majorRepository;

    protected CourseServiceImpl(JpaRepository<Course, Long> repository, CourseRepository courseRepository, MajorRepository majorRepository) {
        super(repository);
        this.courseRepository = courseRepository;
        this.majorRepository = majorRepository;
    }

    @Override
    protected void prePersist(Course course) {
        Major major = majorRepository.findById(course.getMajor().getId())
                .orElseThrow(() -> new EntityNotFoundException("Major not found"));
        if (major.isDeleted()) {
            throw new EntityNotFoundException("Major not found");
        }
        if (courseRepository.findByTitleAndMajor(course.getTitle(), course.getMajor()).isPresent()) {
            throw new AlreadyExistsException("This course already exists in this major!");
        }
        course.setDeleted(false);
    }

    /*@Override
    public void deleteById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        course.setDeleted(true);
        courseRepository.save(course);
    }*/

    @Override
    public List<Course> findAllMajorCourses(String majorName) {
        Major major = majorRepository.findByNameAndDeletedIsFalse(majorName)
                .orElseThrow(() -> new EntityNotFoundException("Major not found"));

        return courseRepository.findByMajorAndDeletedIsFalse(major);
    }

    @Override
    public Course findById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        if (course.isDeleted()) {
            throw new EntityNotFoundException("Course not found");
        }
        return course;
    }

    @Override
    public List<Course> findAll() {
        return courseRepository.findByDeletedIsFalse();
    }

    @Override
    public Course update(Long id, Course course) {
        Course foundedCourse = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        if (!foundedCourse.isDeleted()) {
            foundedCourse.setTitle(course.getTitle());
            foundedCourse.setMajor(course.getMajor());
            foundedCourse.setUnit(course.getUnit());
            foundedCourse.setDescription(course.getDescription());
            return courseRepository.save(foundedCourse);
        } else {
            throw new EntityNotFoundException("Course not found");
        }
    }
}
