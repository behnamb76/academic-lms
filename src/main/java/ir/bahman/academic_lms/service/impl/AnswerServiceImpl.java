package ir.bahman.academic_lms.service.impl;

import ir.bahman.academic_lms.factory.AnswerFactory;
import ir.bahman.academic_lms.model.Option;
import ir.bahman.academic_lms.model.answer.Answer;
import ir.bahman.academic_lms.repository.OptionRepository;
import ir.bahman.academic_lms.service.AnswerService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class AnswerServiceImpl extends BaseServiceImpl<Answer, Long> implements AnswerService {
    private final AnswerFactory answerFactory;
    private final OptionRepository optionRepository;

    protected AnswerServiceImpl(JpaRepository<Answer, Long> repository, AnswerFactory answerFactory, OptionRepository optionRepository) {
        super(repository);
        this.answerFactory = answerFactory;
        this.optionRepository = optionRepository;
    }

    @Override
    public void saveAnswer(String type, Answer answer, Option option, String answerText) {
        Answer answerForSave = answerFactory.createAnswer(type , answer, option, answerText);
        this.persist(answerForSave);
    }

    @Override
    public Option findOptionById(Long id) {
        return optionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("option not found"));
    }

    @Override
    public Answer update(Long id, Answer answer) {
        return null;
    }
}
