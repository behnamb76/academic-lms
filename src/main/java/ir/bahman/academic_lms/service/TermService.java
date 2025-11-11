package ir.bahman.academic_lms.service;

import ir.bahman.academic_lms.model.AcademicCalender;
import ir.bahman.academic_lms.model.Term;

import java.util.List;

public interface TermService extends BaseService<Term, Long> {
    AcademicCalender findTermCalenderByTermId(Long id);
}
