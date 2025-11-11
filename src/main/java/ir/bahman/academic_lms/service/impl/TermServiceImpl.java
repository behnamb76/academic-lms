package ir.bahman.academic_lms.service.impl;

import ir.bahman.academic_lms.exception.AccessDeniedException;
import ir.bahman.academic_lms.exception.AlreadyExistsException;
import ir.bahman.academic_lms.model.AcademicCalender;
import ir.bahman.academic_lms.model.Term;
import ir.bahman.academic_lms.repository.TermRepository;
import ir.bahman.academic_lms.service.TermService;
import ir.bahman.academic_lms.util.SemesterUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TermServiceImpl extends BaseServiceImpl<Term, Long> implements TermService {
    private final TermRepository termRepository;

    protected TermServiceImpl(JpaRepository<Term, Long> repository, TermRepository termRepository) {
        super(repository);
        this.termRepository = termRepository;
    }

    @Override
    protected void prePersist(Term term) {
        LocalDate now = LocalDate.now();

        term.setYear(term.getAcademicCalender().getRegistrationStart().getYear());

        if (term.getYear() < now.getYear()) {
            throw new IllegalArgumentException("Term year cannot be in the past.");
        }

        term.setSemester(SemesterUtil.currentSemester());
        if (termRepository.existsBySemesterAndMajor(term.getSemester(), term.getMajor())
                && term.getYear() == now.getYear()) {
            throw new AlreadyExistsException(String.format("Term already exists for this major in the %s semester.", term.getSemester()));
        }
        term.setDeleted(false);
    }

    @Override
    public void deleteById(Long id) {
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Term not found"));
        LocalDate now = LocalDate.now();
        if (!now.isBefore(term.getAcademicCalender().getRegistrationStart())) {
            throw new AccessDeniedException("Cannot delete term after registration has started.");
        }
        term.setDeleted(true);
        termRepository.save(term);
    }

    @Override
    public Term update(Long id, Term term) {
        Term foundedTerm = termRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Term not found"));
        if (foundedTerm.isDeleted()){
            throw new EntityNotFoundException("Term not found");
        }
        foundedTerm.setYear(term.getYear());
        foundedTerm.setSemester(term.getSemester());
        foundedTerm.setAcademicCalender(term.getAcademicCalender());
        foundedTerm.setMajor(term.getMajor());
        return termRepository.save(foundedTerm);
    }

    @Override
    public List<Term> findAll() {
        return termRepository.findAllByDeletedIsFalse();
    }

    @Override
    public Term findById(Long id) {
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Term not found"));
        if (term.isDeleted()) {
            throw new EntityNotFoundException("Term not found");
        }
        return term;
    }

    @Override
    public AcademicCalender findTermCalenderByTermId(Long id) {
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Term not found"));
        return term.getAcademicCalender();
    }
}
