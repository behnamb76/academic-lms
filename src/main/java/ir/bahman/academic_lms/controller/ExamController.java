package ir.bahman.academic_lms.controller;

import ir.bahman.academic_lms.dto.AnswerDTO;
import ir.bahman.academic_lms.dto.ExamDTO;
import ir.bahman.academic_lms.dto.GradingDTO;
import ir.bahman.academic_lms.mapper.AnswerMapper;
import ir.bahman.academic_lms.mapper.ExamMapper;
import ir.bahman.academic_lms.model.Exam;
import ir.bahman.academic_lms.model.Option;
import ir.bahman.academic_lms.model.answer.Answer;
import ir.bahman.academic_lms.service.AnswerService;
import ir.bahman.academic_lms.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamController {
    private final ExamService examService;
    private final ExamMapper examMapper;
    private final AnswerService answerService;
    private final AnswerMapper answerMapper;

    public ExamController(ExamService examService, ExamMapper examMapper, AnswerService answerService, AnswerMapper answerMapper) {
        this.examService = examService;
        this.examMapper = examMapper;
        this.answerService = answerService;
        this.answerMapper = answerMapper;
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping
    public ResponseEntity<ExamDTO> createExam(@Valid @RequestBody ExamDTO dto) {
        Exam exam = examService.persist(examMapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(examMapper.toDto(exam));
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{id}")
    public ResponseEntity<ExamDTO> updateExam1(@PathVariable Long id,@Valid @RequestBody ExamDTO dto) {
        Exam updated = examService.update(id, examMapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.OK).body(examMapper.toDto(updated));
    }

    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/{id}")
    public ResponseEntity<ExamDTO> findExamById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(examMapper.toDto(examService.findById(id)));
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping
    public ResponseEntity<List<ExamDTO>> findAllExams() {
        List<ExamDTO> dtoList = examService.findAll().stream()
                .map(examMapper::toDto)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(dtoList);
    }

    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    @GetMapping("/course-exams/{courseId}")
    public ResponseEntity<List<ExamDTO>> findAllExamsOfACourse(@PathVariable Long courseId) {
        List<ExamDTO> dtoList = examService.findAllExamOfACourse(courseId).stream()
                .map(examMapper::toDto)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(dtoList);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/start-exam/{examId}")
    public ResponseEntity<Void> studentStartExam(@PathVariable Long examId, Principal principal) {
        examService.startExam(examId, principal);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/submit-exam/{examId}")
    public ResponseEntity<Void> studentSubmitExam(@PathVariable Long examId, Principal principal) {
        examService.submitExam(examId, principal);
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/submit-answer")
    public ResponseEntity<Void> submitAnswer(@Valid  @RequestBody AnswerDTO answerDTO , Principal principal) {
        Answer answer = answerMapper.toEntity(answerDTO , principal);
        Option option = new Option();
        if (answerDTO.getOptionId() != null) {
            option = answerService.findOptionById(answerDTO.getOptionId());
        }
        answerService.saveAnswer(answerDTO.getType() , answer, option, answerDTO.getAnswerText());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/grading-essay")
    public ResponseEntity<Void> gradingEssayQuestionOfExam(@RequestBody GradingDTO dto) {
        examService.essayGrading(dto.getExamId(), dto.getStudentId() , dto.getQuestionId() ,  dto.getScore());
        return ResponseEntity.ok().build();
    }
}
