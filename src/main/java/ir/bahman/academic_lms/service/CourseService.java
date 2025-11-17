package ir.bahman.academic_lms.service;

import ir.bahman.academic_lms.model.Course;

import java.util.List;

public interface CourseService extends BaseService<Course, Long> {
    List<Course> findAllMajorCourses(String majorName);
}
