package ir.bahman.academic_lms.service.impl;

import ir.bahman.academic_lms.factory.QuestionFactory;
import ir.bahman.academic_lms.model.Course;
import ir.bahman.academic_lms.model.Exam;
import ir.bahman.academic_lms.model.ExamQuestion;
import ir.bahman.academic_lms.model.Option;
import ir.bahman.academic_lms.model.question.Question;
import ir.bahman.academic_lms.repository.CourseRepository;
import ir.bahman.academic_lms.repository.ExamQuestionRepository;
import ir.bahman.academic_lms.repository.ExamRepository;
import ir.bahman.academic_lms.repository.QuestionRepository;
import ir.bahman.academic_lms.service.QuestionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public class QuestionServiceImpl extends BaseServiceImpl<Question, Long> implements QuestionService {
    private final QuestionFactory questionFactory;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final CourseRepository courseRepository;

    protected QuestionServiceImpl(JpaRepository<Question, Long> repository, QuestionFactory questionFactory, ExamRepository examRepository, QuestionRepository questionRepository, ExamQuestionRepository examQuestionRepository, CourseRepository courseRepository) {
        super(repository);
        this.questionFactory = questionFactory;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.courseRepository = courseRepository;
    }

    public Question create(String type, Question question, List<Option> options) {
        Question q = questionFactory.createQuestion(type, question, options);
        return persist(q);
    }

    @Override
    public Question update(Long id, Question question) {
        Question foundedQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));
        foundedQuestion.setTitle(question.getTitle());
        foundedQuestion.setText(question.getText());
        foundedQuestion.setDefaultScore(question.getDefaultScore());
        return questionRepository.save(foundedQuestion);
    }

    @Override
    protected void prePersist(Question question) {
        if (question.getTitle() == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }
        if (question.getText() == null) {
            throw new IllegalArgumentException("Question text cannot be null");
        }
        if (question.getDefaultScore() == 0) {
            throw new IllegalArgumentException("Default score cannot be null");
        }
    }

    @Override
    public List<Question> findQuestionsByExamId(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found!"));
        if (exam.isDeleted()) {
            throw new EntityNotFoundException("Exam has been deleted.");
        }
        return questionRepository.findByExamQuestions_Exam_Id(examId);
    }

    @Override
    public List<Question> findQuestionsOfCourse(Long courseId, Principal principal) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found!"));
        if (course.isDeleted()) {
            throw new EntityNotFoundException("Course has been deleted.");
        }
        return questionRepository.findByCourse_Id(courseId);
    }

    @Transactional
    @Override
    public void assignQuestionToExam(Long examId, Long questionId, Double score) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Exam not found!"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found!"));

        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExam(exam);
        examQuestion.setQuestion(question);
        if (score == 0) examQuestion.setScore(question.getDefaultScore());
        else examQuestion.setScore(score);


        examQuestionRepository.save(examQuestion);
        questionRepository.save(question);

        calculateTotalScore(exam);
        examRepository.save(exam);
    }

    public void calculateTotalScore(Exam exam) {
        double totalScore = 0.0;
        for (ExamQuestion examQuestion : exam.getExamQuestions()) {
            Double score = examQuestion.getScore();
            if (score == null) {
                score = examQuestion.getQuestion().getDefaultScore();
            }
            totalScore += score != null ? score : 0.0;
        }
        exam.setScore(totalScore);
    }
}
