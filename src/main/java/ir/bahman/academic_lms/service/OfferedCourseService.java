package ir.bahman.academic_lms.service;

import ir.bahman.academic_lms.model.OfferedCourse;

import java.security.Principal;
import java.util.List;

public interface OfferedCourseService extends BaseService<OfferedCourse, Long> {
    List<OfferedCourse> findAllTeacherCourse(Principal principal);

    List<OfferedCourse> findAllStudentCourses(Principal principal);

    List<OfferedCourse> findAllTermCourses(Long termId, Principal principal);

}
