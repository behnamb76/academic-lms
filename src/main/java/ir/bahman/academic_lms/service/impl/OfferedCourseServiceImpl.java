package ir.bahman.academic_lms.service.impl;

import ir.bahman.academic_lms.exception.AccessDeniedException;
import ir.bahman.academic_lms.model.Account;
import ir.bahman.academic_lms.model.OfferedCourse;
import ir.bahman.academic_lms.model.Person;
import ir.bahman.academic_lms.model.Term;
import ir.bahman.academic_lms.repository.AccountRepository;
import ir.bahman.academic_lms.repository.OfferedCourseRepository;
import ir.bahman.academic_lms.repository.TermRepository;
import ir.bahman.academic_lms.service.OfferedCourseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Service
public class OfferedCourseServiceImpl extends BaseServiceImpl<OfferedCourse, Long> implements OfferedCourseService {
    private final AccountRepository accountRepository;
    private final TermRepository termRepository;
    private final OfferedCourseRepository offeredCourseRepository;

    protected OfferedCourseServiceImpl(JpaRepository<OfferedCourse, Long> repository, AccountRepository accountRepository, TermRepository termRepository, OfferedCourseRepository offeredCourseRepository) {
        super(repository);
        this.accountRepository = accountRepository;
        this.termRepository = termRepository;
        this.offeredCourseRepository = offeredCourseRepository;
    }

    @Override
    protected void prePersist(OfferedCourse offeredCourse) {

        if (offeredCourseRepository.existsOverlappingCourse(
                offeredCourse.getTeacher(),
                offeredCourse.getTerm(),
                offeredCourse.getStartTime(),
                offeredCourse.getMeetingDay(),
                offeredCourse.getEndTime())) {
            throw new AccessDeniedException("Overlapping course detected");
        }
        if (offeredCourse.getStartTime() == null || offeredCourse.getEndTime() == null) {
            throw new IllegalArgumentException("Offered course start time and end time cannot be null");
        }
        if (offeredCourse.getStartTime().isAfter(offeredCourse.getEndTime())) {
            throw new IllegalArgumentException("Offered course start time must be before end time");
        }
    }

    @Override
    public OfferedCourse update(Long id, OfferedCourse offeredCourse) {
        OfferedCourse foundedOfferedCourse = offeredCourseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Offered course not found"));

        LocalDate termStartDate = foundedOfferedCourse.getTerm().getAcademicCalender().getRegistrationStart();
        if (termStartDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot update offered course after the term start date");
        }
        /*Rule.check(termStartDate.isBefore(LocalDate.now()), "Cannot update offered course after the term start date");*/
        foundedOfferedCourse.setStartTime(offeredCourse.getStartTime());
        foundedOfferedCourse.setEndTime(offeredCourse.getEndTime());
        foundedOfferedCourse.setCapacity(offeredCourse.getCapacity());
        return offeredCourseRepository.save(foundedOfferedCourse);
    }

    @Override
    public List<OfferedCourse> findAllTeacherCourse(Principal principal) {
        String username = principal.getName();
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        Person person = account.getPerson();
        return offeredCourseRepository.findAllByTeacher(person);
    }

    @Override
    public List<OfferedCourse> findAllStudentCourses(Principal principal) {
        Account account = accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        Person person = account.getPerson();
        return offeredCourseRepository.findByStudents_Id(person.getId());
    }

    @Override
    public List<OfferedCourse> findAllTermCourses(Long termId, Principal principal) {
        Account account = accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new EntityNotFoundException("Term not found"));

        if (!account.getPerson().getMajor().getName().equals(term.getMajor().getName())) {
            throw new AccessDeniedException("You don't have permission to access this term");
        }
        return offeredCourseRepository.findAllByTerm(term);
    }
}
