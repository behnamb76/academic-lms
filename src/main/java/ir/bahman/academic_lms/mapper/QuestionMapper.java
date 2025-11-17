package ir.bahman.academic_lms.mapper;

import ir.bahman.academic_lms.dto.OptionDTO;
import ir.bahman.academic_lms.dto.QuestionDTO;
import ir.bahman.academic_lms.model.Course;
import ir.bahman.academic_lms.model.Major;
import ir.bahman.academic_lms.model.Option;
import ir.bahman.academic_lms.model.question.EssayQuestion;
import ir.bahman.academic_lms.model.question.Question;
import ir.bahman.academic_lms.model.question.TestQuestion;
import ir.bahman.academic_lms.repository.CourseRepository;
import ir.bahman.academic_lms.repository.MajorRepository;
import ir.bahman.academic_lms.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class QuestionMapper implements BaseMapper<Question, QuestionDTO> {
    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private OptionMapper optionMapper;

    public abstract Question toEntity(QuestionDTO dto);

    public abstract QuestionDTO toDto(Question question);

    @AfterMapping
    protected void afterToEntity(QuestionDTO dto, @MappingTarget Question entity) {
        if (dto.getCourseName() != null && dto.getMajorName() != null) {
            Major major = majorRepository
                    .findByName(dto.getMajorName())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Major with name " + dto.getMajorName() + " not found"
                    ));
            Course course = courseRepository.findByTitleAndMajor(dto.getCourseName(), major)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Course with name " + dto.getCourseName() + " not found in " + major.getName() + " major"
                    ));

            entity.setCourse(course);
        }
    }

    @AfterMapping
    protected void afterToDTO(Question entity, @MappingTarget QuestionDTO dto) {
        if (entity.getCourse() != null) {
            dto.setCourseName(entity.getCourse().getTitle());
            dto.setMajorName(entity.getCourse().getMajor().getName());
        }
        if (entity instanceof TestQuestion testQuestion) {
            dto.setQuestionType("TEST");
            List<OptionDTO> optionDTOList = testQuestion.getOptions().stream()
                    .map(optionMapper::toDto)
                    .collect(Collectors.toList());
            dto.setOptions(optionDTOList);
        } else if (entity instanceof EssayQuestion) {
            dto.setQuestionType("ESSAY");
        }

    }
}
