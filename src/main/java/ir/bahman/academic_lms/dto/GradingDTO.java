package ir.bahman.academic_lms.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradingDTO {
    @NotNull(message = "Exam ID is required")
    @Min(value = 1, message = "Exam ID must be positive")
    private Long examId;

    @NotNull(message = "Student ID is required")
    @Min(value = 1, message = "Student ID must be positive")
    private Long studentId;

    @NotNull(message = "Question ID is required")
    @Min(value = 1, message = "Question ID must be positive")
    private Long questionId;

    @DecimalMin(value = "0.0", inclusive = true, message = "Score must be zero or positive")
    @DecimalMax(value = "100.0", message = "Score cannot exceed maximum possible score")
    private Double score;
}
