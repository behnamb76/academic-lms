package ir.bahman.academic_lms.service.impl;

import ir.bahman.academic_lms.exception.AccessDeniedException;
import ir.bahman.academic_lms.exception.ExamNotActiveException;
import ir.bahman.academic_lms.model.*;
import ir.bahman.academic_lms.model.answer.Answer;
import ir.bahman.academic_lms.model.answer.TestAnswer;
import ir.bahman.academic_lms.model.enums.ExamInstanceStatus;
import ir.bahman.academic_lms.model.enums.ExamState;
import ir.bahman.academic_lms.model.question.Question;
import ir.bahman.academic_lms.repository.*;
import ir.bahman.academic_lms.service.ExamService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl extends BaseServiceImpl<Exam, Long> implements ExamService {
    private final ExamRepository examRepository;
    private final OfferedCourseRepository offeredCourseRepository;
    private final PersonRepository personRepository;
    private final ExamInstanceRepository examInstanceRepository;
    private final AccountRepository accountRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    protected ExamServiceImpl(JpaRepository<Exam, Long> repository, ExamRepository examRepository, OfferedCourseRepository offeredCourseRepository, PersonRepository personRepository, ExamInstanceRepository examInstanceRepository, AccountRepository accountRepository, ExamQuestionRepository examQuestionRepository, AnswerRepository answerRepository, QuestionRepository questionRepository) {
        super(repository);
        this.examRepository = examRepository;
        this.offeredCourseRepository = offeredCourseRepository;
        this.personRepository = personRepository;
        this.examInstanceRepository = examInstanceRepository;
        this.accountRepository = accountRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    protected void prePersist(Exam exam) {
        LocalDateTime now = LocalDateTime.now();
        if (exam.getStartTime() == null || exam.getEndTime() == null) {
            throw new IllegalArgumentException("Exam start time and end time cannot be null");
        }
        if (exam.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Exam start time must be in the future");
        }
        if (exam.getEndTime().isBefore(now)) {
            throw new IllegalArgumentException("Exam end time must be in the future");
        }
        if (exam.getStartTime().isAfter(exam.getEndTime())) {
            throw new IllegalArgumentException("Exam start time must be before end time");
        }
        exam.setDeleted(false);

        if (now.isBefore(exam.getStartTime())) {
            exam.setExamState(ExamState.NOT_STARTED);

        } else if (now.isBefore(exam.getEndTime())) {
            exam.setExamState(ExamState.STARTED);

        } else {
            exam.setExamState(ExamState.FINISHED);
        }
    }

    @Override
    public List<Exam> findAllExamOfACourse(Long courseId) {
        OfferedCourse offeredCourse = offeredCourseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        return offeredCourse.getExams().stream()
                .filter(exam -> !exam.isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public void startExam(Long examId, Principal principal) {
        Account account = accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        Person person = account.getPerson();

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found"));

        Optional<ExamInstance> foundedExamInstance = examInstanceRepository.findByPersonAndExam(person, exam);

        if (foundedExamInstance.isPresent() && foundedExamInstance.get().getStatus() == (ExamInstanceStatus.COMPLETED)) {
            throw new AccessDeniedException("You already complete this exam!");
        }

        if (!personRepository.existsByIdAndOfferedCourses_Id(person.getId(), exam.getOfferedCourse().getId())) {
            throw new AccessDeniedException("You don't have the course for access to start this exam!");
        }
        if (exam.getExamState() == (ExamState.NOT_STARTED)) {
            throw new ExamNotActiveException("Exam not start yet!");
        } else if (exam.getExamState().equals(ExamState.FINISHED)) {
            throw new ExamNotActiveException("Exam time is expired");
        } else if (exam.getExamState().equals(ExamState.STARTED)) {
            LocalDateTime now = LocalDateTime.now();
            ExamInstance examInstance = ExamInstance.builder()
                    .exam(exam)
                    .person(person)
                    .startAt(now)
                    .status(ExamInstanceStatus.IN_PROGRESS)
                    .totalScore(0.0)
                    .build();

            examInstanceRepository.save(examInstance);
        }
    }

    @Override
    public void submitExam(Long examId, Principal principal) {
        Account account = accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new AccessDeniedException("Exam start time and end time cannot be null"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Student with this id not found : " + examId));

        ExamInstance studentExam = examInstanceRepository.findByPersonAndExam(account.getPerson(), exam)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found for this student"));

        if (studentExam.getStatus() == ExamInstanceStatus.COMPLETED) {
            throw new AccessDeniedException("You have already submitted this exam!");
        }
        studentExam.setStatus(ExamInstanceStatus.COMPLETED);
        studentExam.setEndAt(LocalDateTime.now());

        autoTestGrading(examId, account.getPerson().getId());

        examInstanceRepository.save(studentExam);
    }

    @Override
    public Exam findById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found"));
        if (exam.isDeleted()) {
            throw new EntityNotFoundException("Exam is deleted");
        }
        return exam;
    }

    @Override
    public List<Exam> findAll() {
        return examRepository.findByDeletedIsFalse();
    }

    @Override
    public Exam update(Long id, Exam exam) {
        Exam foundedExam = examRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found"));

        if (foundedExam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot update exam after it has started");
        }

        prePersist(exam);

        foundedExam.setTitle(exam.getTitle());
        foundedExam.setDescription(exam.getDescription());
        foundedExam.setExamState(exam.getExamState());
        foundedExam.setStartTime(exam.getStartTime());
        foundedExam.setEndTime(exam.getEndTime());
        return examRepository.save(foundedExam);
    }

    @Override
    public void autoTestGrading(Long examId, Long studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam Instance Not Found"));

        Person person = personRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Person Not Found"));

        ExamInstance examInstance = examInstanceRepository.findByPersonAndExam(person, exam)
                .orElseThrow(() -> new EntityNotFoundException("Exam Instance Not Found"));

        if (examInstance.getAnswers() != null) {
            List<Answer> answers = examInstance.getAnswers();
            for (Answer answer : answers) {
                if (answer instanceof TestAnswer) {
                    if (((TestAnswer) answer).getOption().isCorrect()) {
                        ExamQuestion examQuestion = examQuestionRepository.findByExamAndQuestion(exam, answer.getExamQuestion().getQuestion())
                                .orElseThrow(() -> new EntityNotFoundException("Question Not Found"));
                        Double questionScore = examQuestion.getScore();
                        if (questionScore == null) {
                            questionScore = examQuestion.getQuestion().getDefaultScore();
                        }
                        answer.setScore(questionScore);
                    } else {
                        answer.setScore(0.0);
                    }
                }
                answerRepository.save(answer);
                double score = answer.getScore();
                examInstance.setTotalScore(examInstance.getTotalScore() + score);
            }
        }
        examInstanceRepository.save(examInstance);
    }

    @Override
    public void essayGrading(Long examId, Long studentId, Long questionId, Double score) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam Instance Not Found"));

        Person person = personRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Person Not Found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question Not Found"));

        ExamQuestion examQuestion = examQuestionRepository.findByExamAndQuestion(exam, question)
                .orElseThrow(() -> new EntityNotFoundException("Question Not Found"));

        ExamInstance examInstance = examInstanceRepository.findByPersonAndExam(person, exam)
                .orElseThrow(() -> new EntityNotFoundException("Exam Instance Not Found"));

        Answer answer = answerRepository.findByExamQuestionAndExamInstance(examQuestion, examInstance)
                .orElseThrow(() -> new EntityNotFoundException("Answer Not Found"));

        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
        answer.setScore(score);
        answerRepository.save(answer);

        examInstance.setTotalScore(examInstance.getTotalScore() + score);
        examInstanceRepository.save(examInstance);
    }
}
