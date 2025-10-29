package ir.bahman.academic_lms.model.answer;

import ir.bahman.academic_lms.model.Option;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DiscriminatorValue("TEST_ANSWER")
public class TestAnswer extends Answer {
    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option option;
}
