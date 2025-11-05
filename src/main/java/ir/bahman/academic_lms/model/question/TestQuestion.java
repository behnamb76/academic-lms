package ir.bahman.academic_lms.model.question;

import ir.bahman.academic_lms.model.Option;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DiscriminatorValue("TEST")
public class TestQuestion extends Question {
    @OneToMany(mappedBy = "testQuestion", cascade = CascadeType.ALL)
    private List<Option> options = new ArrayList<>();
}
