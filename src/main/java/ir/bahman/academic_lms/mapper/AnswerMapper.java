package ir.bahman.academic_lms.mapper;

import ir.bahman.academic_lms.dto.AnswerDTO;
import ir.bahman.academic_lms.model.Account;
import ir.bahman.academic_lms.model.Exam;
import ir.bahman.academic_lms.model.ExamInstance;
import ir.bahman.academic_lms.model.ExamQuestion;
import ir.bahman.academic_lms.model.answer.Answer;
import ir.bahman.academic_lms.model.question.Question;
import ir.bahman.academic_lms.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Principal;

@Mapper(componentModel = "spring")
public abstract class AnswerMapper implements BaseMapper<Answer, AnswerDTO> {
    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ExamQuestionRepository examQuestionRepository;

    @Autowired
    private ExamInstanceRepository examInstanceRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public abstract AnswerDTO toDto(Answer answer);

    public abstract Answer toEntity(AnswerDTO dto, @Context Principal principal);

    @AfterMapping
    protected void afterToEntity(AnswerDTO dto, @MappingTarget Answer answer , @Context Principal principal) {

        Exam exam = examRepository.findById(dto.getExamId())
                .orElseThrow(() -> new EntityNotFoundException("Exam id " + dto.getExamId() + " not found."));

        Account account = accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Username " + principal.getName() + " not found."));

        ExamInstance examInstance = examInstanceRepository.findByPersonAndExam(account.getPerson(), exam)
                .orElseThrow(() -> new EntityNotFoundException("Exam id " + dto.getExamId() + " not found."));

        Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Question id " + dto.getQuestionId() + " not found."));

        ExamQuestion examQuestion = examQuestionRepository.findByExamAndQuestion(exam, question)
                .orElseThrow(() -> new EntityNotFoundException("Exam id " + dto.getExamId() + " not found."));

        answer.setExamInstance(examInstance);
        answer.setExamQuestion(examQuestion);
    }
}
