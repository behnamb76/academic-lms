package ir.bahman.academic_lms.controller;

import ir.bahman.academic_lms.dto.ExamQuestionDTO;
import ir.bahman.academic_lms.dto.QuestionDTO;
import ir.bahman.academic_lms.mapper.OptionMapper;
import ir.bahman.academic_lms.mapper.QuestionMapper;
import ir.bahman.academic_lms.model.Option;
import ir.bahman.academic_lms.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/question")
public class QuestionController {
    private final QuestionService questionService;
    private final QuestionMapper questionMapper;
    private final OptionMapper optionMapper;

    public QuestionController(QuestionService questionService, QuestionMapper questionMapper, OptionMapper optionMapper) {
        this.questionService = questionService;
        this.questionMapper = questionMapper;
        this.optionMapper = optionMapper;
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping
    public ResponseEntity<QuestionDTO> createQuestion(@Valid @RequestBody QuestionDTO dto) {
        List<Option> options = new ArrayList<>();
        if (!(dto.getOptions() == null)) {
            dto.getOptions().stream().map(optionMapper::toEntity).forEach(options::add);
        }
        QuestionDTO savedDto = questionMapper.toDto(questionService.create(dto.getQuestionType(), questionMapper.toEntity(dto), options));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/assign-exam")
    public ResponseEntity<Void> assignQuestionToExam(@Valid @RequestBody ExamQuestionDTO dto) {
        questionService.assignQuestionToExam(dto.getExamId(), dto.getQuestionId(), dto.getScore());
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasAnyRole('TEACHER' , 'STUDENT')")
    @GetMapping("/exam-questions/{examId}")
    public ResponseEntity<List<QuestionDTO>> getAllQuestionsOfAExam(@PathVariable Long examId) {
        List<QuestionDTO> dtoList = questionService.findQuestionsByExamId(examId).stream()
                .map(questionMapper::toDto)
                .toList();
        return ResponseEntity.ok().body(dtoList);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/course-questions/{courseId}")
    public ResponseEntity<List<QuestionDTO>> getAllQuestionsOfACourse(@PathVariable Long courseId, Principal principal) {
        List<QuestionDTO> dtoList = questionService.findQuestionsOfCourse(courseId, principal).stream()
                .map(questionMapper::toDto)
                .toList();
        return ResponseEntity.ok().body(dtoList);
    }

}
